
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

    //@Test
    public void loadSlrSource() throws Exception {
        SlrMigrationEJBLocal instance = (SlrMigrationEJBLocal) getEJBInstance(SLR_MODULE_NAME,
                SlrMigrationEJB.class.getSimpleName());
        getUserTransaction().begin();

        //String result = instance.transferSlrSource(false, null, null);
        String result = instance.loadSource();
        getUserTransaction().commit();
        System.out.println(result);
    }

    //@Test
    public void checkSQL() {
        //System.out.println(SlrMigrationSqlProvider.buildGetSlrParcelSql(null, null));
        System.out.println(SlrMigrationSqlProvider.buildUpdateSlrParcelSql());
        System.out.println(SlrMigrationSqlProvider.buildInsertSpatialUnitSql());
        System.out.println(SlrMigrationSqlProvider.buildInsertCadastreObjectSql());
        System.out.println(SlrMigrationSqlProvider.buildInsertSpatialValueAreaSql("officialArea"));
        System.out.println(SlrMigrationSqlProvider.buildInsertAddressSql());
        System.out.println(SlrMigrationSqlProvider.buildInsertParcelAddressSql());
        System.out.println(SlrMigrationSqlProvider.buildUpdateCadastreObjectSql());
        System.out.println(SlrMigrationSqlProvider.buildUpdateSpatialValueAreaSql("officialArea"));

    }

    @Test
    public void loadSlrParcel() throws Exception {
        SlrMigrationEJBLocal instance = (SlrMigrationEJBLocal) getEJBInstance(SLR_MODULE_NAME,
                SlrMigrationEJB.class.getSimpleName());
        getUserTransaction().begin();

        //String result = instance.transferSlrParcel(null, null);
        //System.out.println(result);
        String result = instance.loadParcel();
        getUserTransaction().commit();
        //System.out.println(result);
    }
}