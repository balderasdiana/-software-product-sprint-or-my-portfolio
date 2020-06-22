// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {


  @Override
  public void init() throws ServletException {
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
      Query query = new Query("Visitor").addSort("time", SortDirection.DESCENDING);
      PreparedQuery results = datastoreService.prepare(query);

      ArrayList<String> visitors = new ArrayList<>();
      for(Entity entity: results.asIterable()){
        String visitorHandle = (String) entity.getProperty("username");
        visitors.add(visitorHandle);
      }
    
      Gson gson = new Gson();
      String json = gson.toJson(visitors);
      response.setContentType("text/html;");
      response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
      //Removes all the spaces from a user's name and appends an 'at sign' to the beginning
      String username = "@" + request.getParameter("username").replaceAll("\\s+","");
      long timestamp = System.currentTimeMillis();
      Entity newVisitor = new Entity("Visitor");
      newVisitor.setProperty("username", username);
      newVisitor.setProperty("time",timestamp);
      
      DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

      datastoreService.put(newVisitor);


      response.sendRedirect("/index.html");
  }
}