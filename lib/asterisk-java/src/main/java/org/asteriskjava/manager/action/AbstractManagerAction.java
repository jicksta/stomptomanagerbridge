/*
 *  Copyright 2004-2006 Stefan Reuter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.asteriskjava.manager.action;

import java.lang.reflect.Method;
import java.util.Map;

import org.asteriskjava.util.ReflectionUtil;

/**
 * This class implements the ManagerAction interface and can serve as base class
 * for your concrete Action implementations.
 * 
 * @author srt
 * @version $Id: AbstractManagerAction.java 951 2008-01-30 23:55:28Z srt $
 * @since 0.2
 */
public abstract class AbstractManagerAction implements ManagerAction
{
    /**
     * Serializable version identifier.
     */
    static final long serialVersionUID = -7667827187378395689L;

    private String actionId;

    public abstract String getAction();

    public String getActionId()
    {
        return actionId;
    }

    public void setActionId(String actionId)
    {
        this.actionId = actionId;
    }

    @Override
    public String toString()
    {
        StringBuffer sb;
        Map<String, Method> getters;

        sb = new StringBuffer(getClass().getName() + "[");
        sb.append("action='").append(getAction()).append("',");
        getters = ReflectionUtil.getGetters(getClass());
        for (String attribute : getters.keySet())
        {
            if ("action".equals(attribute) || "class".equals(attribute))
            {
                continue;
            }

            try
            {
                Object value;
                value = getters.get(attribute).invoke(this);
                sb.append(attribute).append("='").append(value).append("',");
            }
            catch (Exception e) //NOPMD
            {
            }
        }
        sb.append("systemHashcode=").append(System.identityHashCode(this));
        sb.append("]");

        return sb.toString();
    }
}
