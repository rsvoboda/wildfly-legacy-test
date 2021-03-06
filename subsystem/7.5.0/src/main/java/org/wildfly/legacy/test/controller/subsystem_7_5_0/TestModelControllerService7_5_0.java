/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.legacy.test.controller.subsystem_7_5_0;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jboss.as.controller.BootContext;
import org.jboss.as.controller.ControlledProcessState;
import org.jboss.as.controller.Extension;
import org.jboss.as.controller.RunningModeControl;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.extension.ExtensionRegistry;
import org.jboss.as.controller.operations.global.GlobalNotifications;
import org.jboss.as.controller.persistence.ConfigurationPersistenceException;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.model.test.ModelTestModelControllerService;
import org.jboss.as.model.test.ModelTestOperationValidatorFilter;
import org.jboss.as.model.test.StringConfigurationPersister;
import org.jboss.as.repository.ContentReference;
import org.jboss.as.repository.ContentRepository;
import org.jboss.as.server.controller.resources.ServerDeploymentResourceDefinition;
import org.jboss.as.subsystem.test.AdditionalInitialization;
import org.jboss.as.subsystem.test.AdditionalInitializationUtil;
import org.jboss.as.subsystem.test.ControllerInitializer;
import org.jboss.dmr.ModelNode;
import org.jboss.vfs.VirtualFile;

/**
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class TestModelControllerService7_5_0 extends ModelTestModelControllerService {

    private final ExtensionRegistry extensionRegistry;
    private final AdditionalInitialization additionalInit;
    private final ControllerInitializer controllerInitializer;
    private final Extension mainExtension;

    TestModelControllerService7_5_0(final Extension mainExtension, final ControllerInitializer controllerInitializer,
                                    final AdditionalInitialization additionalInit, final RunningModeControl runningModeControl, final ExtensionRegistry extensionRegistry,
                                    final StringConfigurationPersister persister, final ModelTestOperationValidatorFilter validateOpsFilter, final boolean registerTransformers) {
        super(AdditionalInitializationUtil.getProcessType(additionalInit), runningModeControl, extensionRegistry.getTransformerRegistry(), persister, validateOpsFilter,
                DESC_PROVIDER, new ControlledProcessState(true), Controller74x.INSTANCE);
        this.extensionRegistry = extensionRegistry;
        this.additionalInit = additionalInit;
        this.controllerInitializer = controllerInitializer;
        this.mainExtension = mainExtension;
    }

    private static final DescriptionProvider DESC_PROVIDER = new DescriptionProvider() {
        public ModelNode getModelDescription(Locale locale) {
            ModelNode model = new ModelNode();
            model.get("description").set("The test model controller");
            return model;
        }
    };

    @Override
    protected void initCoreModel(Resource rootResource, ManagementResourceRegistration rootRegistration, Resource modelControllerResource) {
        super.initCoreModel(rootResource, rootRegistration, modelControllerResource);
        // register the global notifications so there is no warning that emitted notifications are not described by the resource.
        GlobalNotifications.registerGlobalNotifications(rootRegistration, processType);
    }

    @Override
    protected void initExtraModel(Resource rootResource, ManagementResourceRegistration rootRegistration) {
        rootResource.getModel().get(SUBSYSTEM);

        ManagementResourceRegistration deployments = rootRegistration.registerSubModel(ServerDeploymentResourceDefinition.create(new ContentRepository() {
            @Override
            public byte[] addContent(InputStream inputStream) throws IOException {
                return new byte[0];
            }

            @Override
            public void addContentReference(ContentReference contentReference) {

            }

            @Override
            public VirtualFile getContent(byte[] bytes) {
                return null;
            }

            @Override
            public boolean hasContent(byte[] bytes) {
                return false;
            }

            @Override
            public boolean syncContent(ContentReference contentReference) {
                return false;
            }

            @Override
            public void removeContent(ContentReference contentReference) {

            }

            @Override
            public Map<String, Set<String>> cleanObsoleteContent() {
                return null;
            }

        }, null));

        extensionRegistry.setSubsystemParentResourceRegistrations(rootRegistration, deployments);
        AdditionalInitializationUtil.doExtraInitialization(additionalInit, controllerInitializer, extensionRegistry, rootResource, rootRegistration);
    }


    @Override
    protected void boot(BootContext context) throws ConfigurationPersistenceException {
        try {
            super.boot(context);
        } finally {
            countdownDoneLatch();
        }
    }

    @Override
    protected void preBoot(List<ModelNode> bootOperations, boolean rollbackOnRuntimeFailure) {
        mainExtension.initialize(extensionRegistry.getExtensionContext("Test", true));
    }

}
