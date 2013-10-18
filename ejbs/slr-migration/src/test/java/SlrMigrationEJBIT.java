
/**
 * ******************************************************************************************
 * Copyright (c) 2013 Food and Agriculture Organization of the United Nations
 * (FAO) and the Lesotho Land Administration Authority (LAA). All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,this
 * list of conditions and the following disclaimer. 2. Redistributions in binary
 * form must reproduce the above copyright notice,this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. 3. Neither the names of FAO, the LAA nor the names of
 * its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
import java.util.Date;
import java.util.GregorianCalendar;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sola.services.common.test.AbstractEJBTest;
import org.sola.services.ejb.slrmigration.businesslogic.SlrMigrationEJB;
import org.sola.services.ejb.slrmigration.businesslogic.SlrMigrationEJBLocal;
import org.sola.services.ejb.slrmigration.repository.SlrMigrationSqlProvider;

/**
 *
 * @author solaDev
 */
public class SlrMigrationEJBIT extends AbstractEJBTest {

    private static String SLR_MODULE_NAME = "sola-slr-migration-2.0-20130907";

    public SlrMigrationEJBIT() {
        super();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

     @Test
    public void loadSlrSource() throws Exception {
        SlrMigrationEJBLocal instance = (SlrMigrationEJBLocal) getEJBInstance(SLR_MODULE_NAME,
                SlrMigrationEJB.class.getSimpleName());
        getUserTransaction().begin();

        String result = instance.transferSlrSource(null, true, 
         new GregorianCalendar(2013, 01, 01).getTime(), new GregorianCalendar(2013, 06, 01).getTime());
        // System.out.println(result);
        //String result = instance.loadSource();
        getUserTransaction().commit();
        //System.out.println(result);
    }

    //@Test
    public void checkSQL() {
        //System.out.println(SlrMigrationSqlProvider.buildGetSlrParcelSql("11-1", null, null));
        // System.out.println(SlrMigrationSqlProvider.buildGetSlrSourceSql(true, "11-1", null, null));
        System.out.println(SlrMigrationSqlProvider.buildGetSlrLeaseSql(1, true, new GregorianCalendar(2013, 01, 01).getTime(), new GregorianCalendar(2013, 06, 01).getTime()));
        // System.out.println(SlrMigrationSqlProvider.buildGetSlrPartySql("11-1", true, null, null));


    }

   // @Test
    public void loadSlrParcel() throws Exception {
        SlrMigrationEJBLocal instance = (SlrMigrationEJBLocal) getEJBInstance(SLR_MODULE_NAME,
                SlrMigrationEJB.class.getSimpleName());
        getUserTransaction().begin();

         String result = instance.transferSlrParcel(null, 
                new GregorianCalendar(2013, 1, 1).getTime(), new GregorianCalendar(2013, 1, 9).getTime());
        //System.out.println(result);
        //String result = instance.loadParcel();
        getUserTransaction().commit();
        //System.out.println(result);
    }

    //@Test
    public void loadSlrParty() throws Exception {
        SlrMigrationEJBLocal instance = (SlrMigrationEJBLocal) getEJBInstance(SLR_MODULE_NAME,
                SlrMigrationEJB.class.getSimpleName());
        getUserTransaction().begin();

         String result = instance.transferSlrParty(null, true, 
                new GregorianCalendar(2013, 01, 01).getTime(), new GregorianCalendar(2013, 06, 01).getTime());
        //System.out.println(result);
        getUserTransaction().commit();
        //System.out.println(result);
    }

   // @Test
    public void loadSlrLease() throws Exception {
        SlrMigrationEJBLocal instance = (SlrMigrationEJBLocal) getEJBInstance(SLR_MODULE_NAME,
                SlrMigrationEJB.class.getSimpleName());
        getUserTransaction().begin();

        String result = instance.transferSlrLease(new GregorianCalendar(2013, 10, 01).getTime(), null, false, 
              new GregorianCalendar(2013, 01, 01).getTime(), new GregorianCalendar(2013, 06, 01).getTime());
        //System.out.println(result);
       // String result = instance.loadLeaseAndParty();
        getUserTransaction().commit();
        //System.out.println(result);
    }

  //  @Test
    public void loadRrrSourceLink() throws Exception {
        SlrMigrationEJBLocal instance = (SlrMigrationEJBLocal) getEJBInstance(SLR_MODULE_NAME,
                SlrMigrationEJB.class.getSimpleName());
        getUserTransaction().begin();

        String result = instance.loadRrrSourceLink();
        getUserTransaction().commit();
        //System.out.println(result);
    }
}