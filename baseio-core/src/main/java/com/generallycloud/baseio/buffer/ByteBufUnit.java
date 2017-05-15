/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.baseio.buffer;

public class ByteBufUnit {

	protected int		index		= 0;
	protected int		blockBegin	= 0;
	protected int		blockEnd		= 0;
	protected volatile boolean	free	= true;
	
	@Override
	public String toString() {
		return new StringBuilder()
			.append("index=")
			.append(index)
			.append(",free=")
			.append(free)
			.append(",begin=")
			.append(blockBegin)
			.append(",end=")
			.append(blockEnd)
			.toString();
	}
}
