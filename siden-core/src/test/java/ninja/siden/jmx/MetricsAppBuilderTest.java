/*
 * Copyright 2015 SATO taichi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package ninja.siden.jmx;

import ninja.siden.App;
import ninja.siden.Config;
import ninja.siden.Stoppable;
import ninja.siden.internal.Testing;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

import static org.junit.Assert.assertEquals;

/**
 * @author taichi
 */
public class MetricsAppBuilderTest {

    @BeforeClass
    public static void beforeClass() throws Exception {
        Testing.useALL(App.class);
    }

    MBeanServer server;
    App target;
    Stoppable stopper;

    @Before
    public void setUp() throws Exception {
        this.server = ManagementFactory.getPlatformMBeanServer();
        this.target = App.configure(b -> b.set(Config.ENV, "prod"));
    }

    @After
    public void tearDown() throws Exception {
        this.stopper.stop();
    }

    static int port = 9000;

    protected void listen() {
        this.stopper = this.target.listen(port++);
    }

    @Test
    public void session() throws Exception {
        this.listen();

        ObjectName on = new ObjectName("ninja.siden:type=Session");
        MBeanInfo info = server.getMBeanInfo(on);
        MBeanAttributeInfo attr = info.getAttributes()[0];
        assertEquals("Metrics", attr.getName());
    }

    @Test
    public void global() throws Exception {
        this.listen();

        ObjectName on = new ObjectName("ninja.siden:type=Request,name=Global");
        MBeanInfo info = server.getMBeanInfo(on);
        MBeanAttributeInfo attr = info.getAttributes()[0];
        assertEquals("Metrics", attr.getName());
    }

    @Test
    public void routes() throws Exception {
        target.get("/aaa", (req, res) -> "abc");
        this.listen();

        ObjectName on = new ObjectName(
                "ninja.siden:type=Request,path=\"/aaa\",method=GET");
        MBeanInfo info = server.getMBeanInfo(on);
        MBeanAttributeInfo attr = info.getAttributes()[0];
        assertEquals("Metrics", attr.getName());
    }

    @Test
    public void nestedRoutes() throws Exception {
        App sub = new App();
        sub.head("/def", (req, res) -> "def");
        target.use("/abc", sub);
        this.listen();
        ObjectName abc = new ObjectName(
                "ninja.siden:type=Request,path=\"/abc/def\",method=HEAD");
        server.getMBeanInfo(abc);
    }

    @Test
    public void nestedRoutesTwoTimes() throws Exception {
        App sub = new App();
        sub.head("/def", (req, res) -> "def");
        target.use("/abc", sub);
        target.use("/efg", sub);
        this.listen();

        ObjectName abc = new ObjectName(
                "ninja.siden:type=Request,path=\"/abc/def\",method=HEAD");
        server.getMBeanInfo(abc);

        ObjectName efg = new ObjectName(
                "ninja.siden:type=Request,path=\"/efg/def\",method=HEAD");
        server.getMBeanInfo(efg);
    }

    @Test
    public void deeplyNestedRoutes() throws Exception {
        App subsub = new App();
        subsub.get("/jkl", (req, res) -> "eee");
        App sub = new App();
        sub.head("/def", (req, res) -> "def");
        sub.use("/ghi", subsub);

        target.use("/abc", sub);
        this.listen();

        ObjectName def = new ObjectName(
                "ninja.siden:type=Request,path=\"/abc/def\",method=HEAD");
        server.getMBeanInfo(def);

        ObjectName jkl = new ObjectName(
                "ninja.siden:type=Request,path=\"/abc/ghi/jkl\",method=GET");
        server.getMBeanInfo(jkl);
    }

    @Test
    public void websockets() throws Exception {
        App sub = new App();
        sub.websocket("/ws").onText((c, s) -> c.send(s));
        target.use("/aaa", sub);
        this.listen();

        ObjectName ws = new ObjectName(
                "ninja.siden:type=WebSocket,path=\"/aaa/ws\"");
        server.getMBeanInfo(ws);
    }

}
