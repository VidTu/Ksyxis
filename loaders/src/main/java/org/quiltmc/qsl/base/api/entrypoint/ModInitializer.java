/*
 * Copyright 2022 The Quilt Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.base.api.entrypoint;

import org.quiltmc.loader.api.ModContainer;

/**
 * Stub entrypoint interface of the Quilt mod. This is an injection interface for Ksyxis for Quilt,
 * see {@code KQuilt} class.
 *
 * @author VidTu
 * @author FabricMC
 * @author QuiltMC
 */
public interface ModInitializer {
    /**
     * Runs the entrypoint. This is an injection point for Ksyxis for Quilt, see {@code KQuilt} class.
     *
     * @param mod A stub mod container, ignored
     */
    void onInitialize(ModContainer mod);
}
