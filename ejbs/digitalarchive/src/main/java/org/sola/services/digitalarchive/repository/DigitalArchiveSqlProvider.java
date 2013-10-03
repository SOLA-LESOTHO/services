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
package org.sola.services.digitalarchive.repository;

import static org.apache.ibatis.jdbc.SqlBuilder.*;

/**
 *
 * @author soladev
 */
public class DigitalArchiveSqlProvider {

    public static final String QUERY_PARAM_FILE_ID = "fileId";
    public static final String QUERY_PARAM_VERSION = "version";
    public static final String QUERY_PARAM_DOCUMENT_ID = "documentId";

    /**
     * Retrieves a document record from the SQL Server SLR Lesotho database for
     * display to the user
     *
     * @param includeBody Flag to indicate if the body of the document should be
     * retrieved or not.
     */
    public static String buildGetSlrDocumentSql(boolean includeBody) {
        String result;
        BEGIN();
        SELECT("#{" + QUERY_PARAM_DOCUMENT_ID + "} AS id");
        SELECT("#{" + QUERY_PARAM_DOCUMENT_ID + "} AS nr");
        SELECT("'SLR Document' AS description");
        SELECT("dv.[EXTENSION] AS extension");
        if (includeBody) {
            SELECT("fb.[DATA] AS body");
        }
        SELECT("1 AS rowversion");
        SELECT("'unknown' AS change_user");
        SELECT("#{" + QUERY_PARAM_DOCUMENT_ID + "} AS rowidentifier");
        FROM("dbo.DATAFILEVERSION_BYTES fb");
        FROM("dbo.DOCUMENTFILEVERSION dv");
        WHERE("fb.[ID_DOCUMENTFILEPART] = #{" + QUERY_PARAM_FILE_ID + "}");
        WHERE("fb.[ID_DATAFILEVERSION] = #{" + QUERY_PARAM_VERSION + "}");
        WHERE("dv.[ID_DOCUMENTFILEPART] = fb.[ID_DOCUMENTFILEPART]");
        WHERE("dv.[DATAFILEVERSION] = fb.[ID_DATAFILEVERSION]");

        result = SQL();
        return result;
    }
}
