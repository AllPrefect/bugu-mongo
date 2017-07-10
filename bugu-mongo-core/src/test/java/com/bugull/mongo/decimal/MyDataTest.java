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
package com.bugull.mongo.decimal;

import com.bugull.mongo.base.ReplicaSetBaseTest;
import org.junit.Test;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class MyDataTest extends ReplicaSetBaseTest {
    
    //@Test
    public void testInsert(){
        connectDB();
        
        MyDataDao dao = new MyDataDao();
        MyData data = new MyData();
        data.setF(1000f);
        data.setD(2000d);
        dao.save(data);
        
        disconnectDB();
    }
    
    @Test
    public void testFind(){
        connectDB();
        
        MyDataDao dao = new MyDataDao();
        MyData data = dao.findOne();
        
        disconnectDB();
    }
    
}
