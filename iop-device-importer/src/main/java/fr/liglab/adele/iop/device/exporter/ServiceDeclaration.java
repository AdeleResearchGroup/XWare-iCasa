/**
 *
 *   Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa/snapshot/license.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.iop.device.exporter;

import java.util.List;
import java.util.Map;

import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ExportDeclarationBuilder;

import de.mannheim.wifo2.iop.service.model.ICapability;

public class ServiceDeclaration {

    public final static String SERVICE 				= "iop.exported.service";

    public final static String SERVICE_ID 			= "iop.exported.service.id";

    public final static String SERVICE_CAPABILITIES = "iop.exported.service.capabilities";


    private final String 			id;
    private final List<ICapability> capabilities;
    private final Object			service;
    
	@SuppressWarnings("unchecked")
	public ServiceDeclaration(ExportDeclaration declaration) {
        Map<String,Object> metadata = declaration.getMetadata();
        id 				= (String) metadata.get(SERVICE_ID);
        service			= metadata.get(SERVICE);
        capabilities	= (List<ICapability>) metadata.get(SERVICE_CAPABILITIES);
    }

    public String getId() {
        return id;
    }
    
    public List<ICapability> getCapabilities() {
    	return capabilities;
    }

    public Object getService() {
    	return service;
    }
    
    public static ExportDeclaration from(Object service, String id, List<ICapability> capabilities) {
    	
    	return ExportDeclarationBuilder.empty()
    			.key("scope").value("generic")
    			.key(SERVICE).value(service)
    			.key(SERVICE_ID).value(id)
    			.key(SERVICE_CAPABILITIES).value(capabilities)
    			.build();
    }


}