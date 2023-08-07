function lambda_request_transformation( req )

        local reqBody = req:body();
        local reqHeaders = req:headers("message-headers");
        local messageKey = req:headers("message-key");
		local path = req:path();


    	local jsonHeaders = generate_headers_json(reqHeaders);
    	local topicName = get_topicname_frompath(path)

        local newRequestBody = generate_body_as_json(reqBody,jsonHeaders,topicName,messageKey)
        req:body(newRequestBody);
end


function generate_headers_json(headers)
        if not headers or headers == '' then headers = '{}' end
        headersAsTable = {};
        for key, value in string.gmatch(headers, "(%w+)=(%w+)") do
                 table.insert(headersAsTable, string.format("\"%s\":\"%s\"", key, value))
        end

        return "{" .. table.concat(headersAsTable, ",") .. "}";
end

function generate_body_as_json(body,headers,topic,messageKey)
        bodyAsTable = {};
        if not body or body == '' then body = '{}' end

        table.insert(bodyAsTable, string.format("\"%s\":%s", "body", body));
        table.insert(bodyAsTable, string.format("\"%s\":%s", "headers", headers));
        table.insert(bodyAsTable, string.format("\"%s\":\"%s\"", "topicName", topic));
        if not (not messageKey or  messageKey == '') then
        	table.insert(bodyAsTable, string.format("\"%s\":\"%s\"", "key", messageKey));
        end
        return "{" .. table.concat(bodyAsTable, ",") .. "}";
end

function get_topicname_frompath(path)

	index = string.find(path, "/[^/]*$")
	return path:sub(index+1)

end

function lambda_response_transformation( resp )
    print("response transformation");

    local responseData = resp:data();
    local responseStatusCode = responseData:get("statusCode");

   if not (responseStatusCode == nil) and not (responseStatusCode == '200') then
          local responseMessage = responseData:get("message");
          --TODO validate if message is not nil
          custom_error(responseMessage,tonumber(responseStatusCode));
    end
end

