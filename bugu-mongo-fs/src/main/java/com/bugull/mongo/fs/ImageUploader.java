/*
 * Copyright (c) www.bugull.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bugull.mongo.fs;

import com.bugull.mongo.utils.StreamUtil;
import com.bugull.mongo.utils.StringUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Convenient class for uploading an image file to GridFS.
 * 
 * <p>Besides uploading, ImageUploader can watermark and compress an image.</p>
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class ImageUploader extends Uploader{
    
    public final static String DIMENSION = "dimension";
    
    public ImageUploader(File file, String originalName){
        super(file, originalName);
    }
    
    public ImageUploader(File file, String originalName, boolean rename){
        super(file, originalName, rename);
    }
    
    public ImageUploader(InputStream input, String originalName){
        super(input, originalName);
    }
    
    public ImageUploader(InputStream input, String originalName, boolean rename){
        super(input, originalName, rename);
    }
    
    public ImageUploader(byte[] data, String originalName){
        super(data, originalName);
    }
    
    public ImageUploader(byte[] data, String originalName, boolean rename){
        super(data, originalName, rename);
    }
    
    /**
     * Save the image with a watermark on it.
     * @param watermark 
     * @return the file(with watermark on it) id generated by MongoDB.
     */
    public String save(Watermark watermark){
        processFilename();
        String fid = null;
        if(!StringUtil.isEmpty(watermark.getText())){
            fid = pressText(watermark);
        }
        else if(!StringUtil.isEmpty(watermark.getImagePath())){
            fid = pressImage(watermark);
        }
        else{
            fid = saveInputStream();
        }
        return fid;
    }
    
    private String pressText(Watermark watermark){
        BufferedImage originalImage = openImage(input);
        int originalWidth = originalImage.getWidth(null);
        int originalHeight = originalImage.getHeight(null);
        BufferedImage newImage = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = newImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, originalWidth, originalHeight, null);
        g2d.setColor(watermark.getColor());
        g2d.setFont(new Font(watermark.getFontName(), watermark.getFontStyle(), watermark.getFontSize())); 
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, watermark.getAlpha())); 
        String text = watermark.getText();
        int len = text.length();
        int fontSize = watermark.getFontSize();
        switch(watermark.getAlign()){
            case Watermark.CENTER:
                g2d.drawString(text, (originalWidth - (len * fontSize)) / 2, (originalHeight - fontSize) / 2);
                break;
            case Watermark.BOTTOM_RIGHT:
                g2d.drawString(text, originalWidth - (len * fontSize) - watermark.getRight(), originalHeight - fontSize - watermark.getBottom());
                break;
            case Watermark.BOTTOM_LEFT:
                g2d.drawString(text, watermark.getLeft(), originalHeight - fontSize - watermark.getBottom());
                break;
            default:
                break;
        }
        g2d.dispose();
        return saveImage(newImage);
    }
    
    private String pressImage(Watermark watermark){
        BufferedImage originalImage = openImage(input);
        BufferedImage watermarkImage = openImage(new File(watermark.getImagePath()));
        int originalWidth = originalImage.getWidth(null);
        int originalHeight = originalImage.getHeight(null);
        int watermarkWidth = watermarkImage.getWidth(null);
        int watermarkHeight = watermarkImage.getHeight(null);
        if (originalWidth < watermarkWidth || originalHeight < watermarkHeight) {
            return saveInputStream();
        }
        BufferedImage newImage = new BufferedImage(originalWidth, originalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = newImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, originalWidth, originalHeight, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, watermark.getAlpha())); 
        //position of the watermark
        switch(watermark.getAlign()){
            case Watermark.CENTER:
                g2d.drawImage(watermarkImage, (originalWidth - watermarkWidth) / 2, (originalHeight - watermarkHeight) / 2, watermarkWidth, watermarkHeight, null);
                break;
            case Watermark.BOTTOM_RIGHT:
                g2d.drawImage(watermarkImage, originalWidth - watermarkWidth - watermark.getRight(), originalHeight - watermarkHeight - watermark.getBottom(), watermarkWidth, watermarkHeight, null);
                break;
            case Watermark.BOTTOM_LEFT:
                g2d.drawImage(watermarkImage, watermark.getLeft(), originalHeight - watermarkHeight - watermark.getBottom(), watermarkWidth, watermarkHeight, null);
                break;
            default:
                break;
        }
        g2d.dispose();
        return saveImage(newImage);
    }
    
    /**
     * compress the image to another dimension, not scale to filling
     * @param dimension
     * @param maxWidth
     * @param maxHeight
     * @return the compressed file id generated by MongoDB.
     */
    public String compress(String dimension, int maxWidth, int maxHeight) {
        return compress(dimension, maxWidth, maxHeight, false);
    }
    
    /**
     * compress the image to another dimension
     * @param dimension
     * @param maxWidth
     * @param maxHeight
     * @param filling fill the entire area or not
     * @return the compressed file id generated by MongoDB.
     */
    public String compress(String dimension, int maxWidth, int maxHeight, boolean filling) {
        setAttribute(DIMENSION, dimension);
        int targetWidth = maxWidth;
        int targetHeight = maxHeight;
        BufferedImage srcImage = openImage(getOriginalInputStream());
        if(!filling){
            int srcWidth = srcImage.getWidth(null);
            int srcHeight = srcImage.getHeight(null);
            if(srcWidth <= maxWidth && srcHeight <= maxHeight){
                return saveImage(srcImage);
            }
            double ratio = Math.min((double) maxWidth / srcWidth, (double) maxHeight / srcHeight);
            targetWidth = (int)(srcWidth * ratio);
            targetHeight = (int)(srcHeight * ratio);
        }
        BufferedImage targetImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = targetImage.createGraphics();
        if(FileTypeUtil.isPng(filename)){
            //if it's png image, create compatible image with transparency.
            targetImage = g2d.getDeviceConfiguration().createCompatibleImage(targetWidth, targetHeight, Transparency.TRANSLUCENT);
            g2d.dispose();
            //targetImage have changed, Graphics2D must be created again.
            g2d = targetImage.createGraphics();
        }
        Image img = srcImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        g2d.drawImage(img, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return saveImage(targetImage);
    }
    
    private InputStream getOriginalInputStream(){
        DBObject query = new BasicDBObject(BuguFS.FILENAME, filename);
        query.put(DIMENSION, null);
        BuguFS fs = BuguFSFactory.getInstance().create(connection, bucket, chunkSize);
        GridFSDBFile f = fs.findOne(query);
        return f.getInputStream();
    }
    
    private BufferedImage openImage(File f){
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(f);
        } catch (IOException ex) {
            throw new BuguFSException(ex.getMessage());
        }
        return bi;
    }
    
    private BufferedImage openImage(InputStream is){
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(is);
        } catch (IOException ex) {
            throw new BuguFSException(ex.getMessage());
        } finally {
            StreamUtil.safeClose(is);
        }
        return bi;
    }
    
    private String saveImage(BufferedImage bi) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String ext = "jpg";
        String t = FileTypeUtil.getExtention(filename);
        if(!StringUtil.isEmpty(t)){
            ext = t;
        }
        String fid = null;
        try{
            ImageIO.write(bi, ext, baos);
            BuguFS fs = BuguFSFactory.getInstance().create(connection, bucket, chunkSize);
            fid = fs.save(baos.toByteArray(), filename, attributes);
        }catch(IOException ex){
            throw new BuguFSException(ex.getMessage());
        }finally{
            StreamUtil.safeClose(baos);
        }
        return fid;
    }
    
    /**
     * Get the image's width and height.
     * @return element 0 for width, element 1 for height
     */
    public int[] getSize(){
        int[] size = new int[2];
        BufferedImage image = openImage(getOriginalInputStream());
        size[0] = image.getWidth(null);
        size[1] = image.getHeight(null);
        return size;
    }
    
}
