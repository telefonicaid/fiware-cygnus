/**
 * Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-connectors (FI-WARE project).
 *
 * fiware-connectors is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-connectors is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
 * francisco.romerobueno@telefonica.com
 */

package es.tid.fiware.fiwareconnectors.cygnus.management;

import es.tid.fiware.fiwareconnectors.cygnus.utils.Utils;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

/**
 *
 * @author frb
 */
public class ManagementInterface extends AbstractHandler {
    
    @Override
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
        throws IOException, ServletException {
        
        HttpConnection connection = HttpConnection.getCurrentConnection();
        
        if (connection != null) {
            Request baseRequest = (request instanceof Request) ? (Request) request : connection.getRequest();
            baseRequest.setHandled(true);
        } // if
        
        response.setContentType("text/html;charset=utf-8");
        String uri = request.getRequestURI();
        
        if (uri.equals("/version")) {
            response.setContentType("json;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("{\"version\":\"" + Utils.getCygnusVersion() + "." + Utils.getLastCommit()
                    + "\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println("404 - Not found");
        } // if else
    } // handle
    
} // ManagementInterface
