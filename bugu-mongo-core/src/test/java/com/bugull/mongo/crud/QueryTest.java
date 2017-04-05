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

package com.bugull.mongo.crud;

import com.bugull.mongo.BuguMapper;
import com.bugull.mongo.base.ReplicaSetBaseTest;
import com.bugull.mongo.dao.GroupContactDao;
import com.bugull.mongo.dao.GroupProductDao;
import com.bugull.mongo.dao.OrderDao;
import com.bugull.mongo.dao.ProductDao;
import com.bugull.mongo.dao.UserDao;
import com.bugull.mongo.entity.Address;
import com.bugull.mongo.entity.Contact;
import com.bugull.mongo.entity.GroupContact;
import com.bugull.mongo.entity.GroupProduct;
import com.bugull.mongo.entity.Order;
import com.bugull.mongo.entity.Product;
import com.bugull.mongo.entity.User;
import com.bugull.mongo.utils.SortUtil;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.Test;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class QueryTest extends ReplicaSetBaseTest {
    
    @Test
    public void testQuery(){
        connectDB();
        
        UserDao userDao = new UserDao();
        User user = userDao.query().is("username", "frank").result();
        System.out.println("age: " + user.getAge());
        System.out.println("valid: " + user.isValid());
        System.out.println("register time: " + user.getRegisterTime());
        Contact contact = user.getContact();
        System.out.println("email: " + contact.getEmail());
        System.out.println("phone: " + contact.getPhone());
        List<Address> addressList = user.getAddressList();
        for(Address address : addressList){
            System.out.println("province: " + address.getProvince());
            System.out.println("city: " + address.getCity());
            System.out.println("detail address: " + address.getDetailAddress());
        }
        Map<String, List<Integer>> permissions = user.getPermissions();
        Set<Entry<String, List<Integer>>> set = permissions.entrySet();
        for(Entry<String, List<Integer>> entry : set){
            System.out.println("module: " + entry.getKey());
            List<Integer> list = entry.getValue();
            for(Integer i : list){
                System.out.println("p: " + i);
            }
        }
        float[] scores = user.getScores();
        for(float f : scores){
            System.out.println("score:" + f);
        }
        
        System.out.println();
        
        OrderDao orderDao = new OrderDao();
        List<Order> orderList = orderDao.query().is("user", user).sort(SortUtil.desc("money")).results();
        BuguMapper.fetchCascade(orderList, "productList");
        for(Order order : orderList){
            System.out.println("total money: " + order.getMoney());
            List<Product> productList = order.getProductList();
            for(Product product : productList){
                System.out.println("product name: " + product.getName());
            }
        }
        
        disconnectDB();
    }
    
    //@Test
    public void testComplexEmbedList(){
        connectDB();
        
        GroupContactDao gcDao = new GroupContactDao();
        List<GroupContact> all = gcDao.findAll();
        for(GroupContact gc : all){
            Map<String, Contact> map = gc.getMapContacts();
            for(Entry<String, Contact> entry : map.entrySet()){
                System.out.println("key: " + entry.getKey());
                Contact c = entry.getValue();
                System.out.println("email: " + c.getEmail());
                System.out.println("phone: " + c.getPhone());
            }
            System.out.println();
            Map<String, List<Contact>> mapList = gc.getMapListContacts();
            for(Entry<String, List<Contact>> entry : mapList.entrySet()){
                System.out.println("key: " + entry.getKey());
                List<Contact> list = entry.getValue();
                for(Contact c : list){
                    System.out.println("email: " + c.getEmail());
                    System.out.println("phone: " + c.getPhone());
                }
            }
        }
        
        disconnectDB();
    }
    
    //@Test
    public void testComplextRefList(){
        connectDB();
        
        GroupProductDao gpDao = new GroupProductDao();
        List<GroupProduct> all = gpDao.findAll();
        for(GroupProduct gp : all){
            Map<String, Product> map = gp.getMap();
            for(Entry<String, Product> entry : map.entrySet()){
                System.out.println("key: " + entry.getKey());
                Product p = entry.getValue();
                System.out.println("product name: " + p.getName());
            }
            System.out.println();
            Map<String, List<Product>> mapList = gp.getMapList();
            for(Entry<String, List<Product>> entry : mapList.entrySet()){
                System.out.println("key: " + entry.getKey());
                List<Product> list = entry.getValue();
                for(Product p : list){
                    System.out.println("product name: " + p.getName());
                }
            }
        }
        
        disconnectDB();
    }
    
    //@Test
    public void queryWithInvalidID() throws Exception {
        connectDB();
        
        ProductDao productDao = new ProductDao();
        
        Product p = productDao.findOne("abc");
        
        System.out.println("p==null: " + (p==null));
        
        disconnectDB();
    }
    
    //@Test
    public void testCount(){
        connectDB();

        ProductDao productDao = new ProductDao();
        
        System.out.println("count: " + productDao.count());
        
        disconnectDB();
    }

}
