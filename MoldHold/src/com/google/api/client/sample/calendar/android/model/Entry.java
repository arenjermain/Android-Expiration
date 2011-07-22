/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.sample.calendar.android.model;

import com.google.api.client.util.Data;
import com.google.api.client.util.Key;

import java.util.List;

/**
 * @author Yaniv Inbar
 */
public class Entry implements Cloneable {

  @Key
  public String summary;

  @Key
  public String title;

  @Key
  public String updated;

  @Key("link")
  public List<Link> links;

  @Override
  protected Entry clone() {
    try {
      @SuppressWarnings("unchecked")
      Entry result = (Entry) super.clone();
      Data.deepCopy(this, result);
      return result;
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException(e);
    }
  }

  String getEditLink() {
    return Link.find(links, "edit");
  }
}
