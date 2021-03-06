/*
 * Copyright 2013 Jeanfrancois Arcand
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.wasync.transport;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.RequestBuilder;
import org.atmosphere.wasync.FunctionWrapper;
import org.atmosphere.wasync.Options;
import org.atmosphere.wasync.Request;

import java.util.List;

import static org.atmosphere.wasync.Event.MESSAGE;

/**
 * Long-Polling {@link org.atmosphere.wasync.Transport} implementation
 *
 * @author Jeanfrancois Arcand
 */
public class LongPollingTransport extends StreamTransport {

    public LongPollingTransport(RequestBuilder requestBuilder, Options options, Request request, List<FunctionWrapper> functions) {
        super(requestBuilder, options, request, functions);
    }

    @Override
    public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
        if (isBinary) {
            byte[] payload = bodyPart.getBodyPartBytes();
            if (protocolEnabled && !protocolReceived) {
                if (!whiteSpace(payload)) {
                    TransportsUtil.invokeFunction(decoders, functions, payload.getClass(), payload, MESSAGE.name(), resolver);
                    protocolReceived = true;
                }
                return AsyncHandler.STATE.CONTINUE;
            } else if (!whiteSpace(payload)) {
                TransportsUtil.invokeFunction(decoders, functions, payload.getClass(), payload, MESSAGE.name(), resolver);
            }
        } else {
            String m = new String(bodyPart.getBodyPartBytes(), charSet).trim();
            if (protocolEnabled && !protocolReceived) {
                if (!m.isEmpty()) {
                    TransportsUtil.invokeFunction(decoders, functions, m.getClass(), m, MESSAGE.name(), resolver);
                    protocolReceived = true;
                }
                return AsyncHandler.STATE.CONTINUE;
            } else if (!m.isEmpty()) {
                TransportsUtil.invokeFunction(decoders, functions, m.getClass(), m, MESSAGE.name(), resolver);
            }
        }
        if (connectdFuture != null) connectdFuture.done();
        return AsyncHandler.STATE.CONTINUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Request.TRANSPORT name() {
        return Request.TRANSPORT.LONG_POLLING;
    }

}

