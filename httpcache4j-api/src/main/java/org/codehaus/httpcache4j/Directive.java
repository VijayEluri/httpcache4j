/*
 * Copyright (c) 2010. The Codehaus. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.codehaus.httpcache4j;

import org.codehaus.httpcache4j.util.NumberUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class Directive extends NameValue {
    private final List<Parameter> parameters;
    private Map<String, Parameter> parameterMap;
    
    public Directive(final String name, String value) {
        this(name, HeaderUtils.removeQuotes(value), Collections.<Parameter>emptyList());
    }

    public Directive(final String name, String value, List<Parameter> parameters) {
        super(name, HeaderUtils.removeQuotes(value));
        this.parameters = Objects.requireNonNull(parameters);
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public int getValueAsInteger() {
        return NumberUtils.toInt(getValue(), -1);
    }

    public Parameter getParameter(String name) {
        if (parameterMap == null) {
            synchronized (this) {
                if (parameterMap == null) {
                    parameterMap = parameters.stream().collect(Collectors.toMap(NameValue::getName, Function.<Parameter>identity()));
                }
            }
        }
        return parameterMap.get(name);
    }

    public String getParameterValue(String name) {
        Parameter param = getParameter(name);
        if (param != null) {
            return param.getValue();
        }
        return null;
    }

    @Override
    public String toString() {
        String output = name;
        if (!value.isEmpty()) {
            output += "=" + value;
        }
        if (!parameters.isEmpty()) {
            output = output + "; " + parameters.stream().map(Parameter::toString).collect(Collectors.joining("; "));
        }
        return output;
    }
}
