/**
 *
 * Copyright (c) 2025, Openflexo
 *
 * This file is part of OpenFlexo MCP Technology Adapter, a component of the software infrastructure
 * developed at Openflexo.
 *
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either
 * version 1.1 of the License, or any later version ), which is available at
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 *
 * You can redistribute it and/or modify under the terms of either of these licenses
 *
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or
 *          combining it with software containing parts covered by the terms
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. *
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.openflexo.org/license.html for details.
 *
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 *
 */

package org.openflexo.mcp.protocol;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents an MCP error following JSON-RPC 2.0 error object specification
 *
 * Standard error codes:
 * -32700: Parse error
 * -32600: Invalid request
 * -32601: Method not found
 * -32602: Invalid params
 * -32603: Internal error
 * -32000 to -32099: Server error (reserved for implementation-defined errors)
 *
 * @author Mouad
 */
public class MCPError {

     public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;

     public static final int CONNECTION_ERROR = -32000;
    public static final int TIMEOUT_ERROR = -32001;
    public static final int TRANSPORT_ERROR = -32002;

    private int code;

    private String message;

    private JsonElement data;


    public MCPError(int code, String message) {
        this.code = code;
        this.message = message;
    }


    public MCPError(int code, String message, JsonElement data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }


    public int getCode() {
        return code;
    }


    public void setCode(int code) {
        this.code = code;
    }


    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }


    public JsonElement getData() {
        return data;
    }


    public void setData(JsonElement data) {
        this.data = data;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("code", code);
        json.addProperty("message", message);
        if (data != null) {
            json.add("data", data);
        }
        return json;
    }

    @Override
    public String toString() {
        return String.format("MCPError[code=%d, message='%s']", code, message);
    }

    // Factory methods for common errors

    public static MCPError parseError(String detail) {
        return new MCPError(PARSE_ERROR, "Parse error: " + detail);
    }

    public static MCPError invalidRequest(String detail) {
        return new MCPError(INVALID_REQUEST, "Invalid request: " + detail);
    }

    public static MCPError methodNotFound(String method) {
        return new MCPError(METHOD_NOT_FOUND, "Method not found: " + method);
    }

    public static MCPError invalidParams(String detail) {
        return new MCPError(INVALID_PARAMS, "Invalid params: " + detail);
    }

    public static MCPError internalError(String detail) {
        return new MCPError(INTERNAL_ERROR, "Internal error: " + detail);
    }

    public static MCPError connectionError(String detail) {
        return new MCPError(CONNECTION_ERROR, "Connection error: " + detail);
    }

    public static MCPError timeoutError(String detail) {
        return new MCPError(TIMEOUT_ERROR, "Timeout error: " + detail);
    }
}