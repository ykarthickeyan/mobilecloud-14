/*
 * 
 * Copyright 2014 Jules White
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
 * 
 */

package org.magnum.mobilecloud.video;

import java.security.Principal;
import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
public class Assignment2Controller {
	
	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */
	
	
	// The path where we expect the VideoSvc to live
	public static final String VIDEO_SVC_PATH = "/video";
	
	// The path where we expect the VideoSvc to live
	public static final String VIDEO_SVC_PATH_ID = "/video/{id}";
	
	// Like video path
	public static final String VIDEO_SVC_LIKE_VIDEO = "/video/{id}/like";
	
	// unlike video path
	public static final String VIDEO_SVC_UNLIKE_VIDEO = "/video/{id}/unlike";
	
	// Liked by video path
	public static final String VIDEO_SVC_LIKE_BY_VIDEO = "/video/{id}/likedBy";

	// The path to search videos by title
	public static final String VIDEO_TITLE_SEARCH_PATH = VIDEO_SVC_PATH + "/search/findByName";
	
	// The path to search videos by title
	public static final String VIDEO_DURATION_SEARCH_PATH = VIDEO_SVC_PATH + "/search/findByDurationLessThan";
	
	
	@Autowired
	private VideoRepository videosRepository;

	@RequestMapping(value = VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		Video v1 = videosRepository.save(v);
		return v1;
	}
	
	@RequestMapping(value = VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		Iterable<Video> it1 = videosRepository.findAll();
		return Lists.newArrayList(it1);
	}

	@RequestMapping(value = VIDEO_SVC_PATH_ID, method = RequestMethod.GET)
	public @ResponseBody Video getVideoById(@PathVariable("id") long id) {
		Video v = videosRepository.findOne(id);
		if(v != null){
			return v;	
		}
		else {
			throw new ResourceNotFoundException();
		}
		
	}


	@RequestMapping(value = VIDEO_TITLE_SEARCH_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> findByTitle(@RequestParam("title") String title) {
		Iterable<Video> it1 = videosRepository.findByName(title);
		System.out.println("findByName:"+it1);
		return Lists.newArrayList(it1);
	}

	@RequestMapping(value = VIDEO_DURATION_SEARCH_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(@RequestParam("duration") long duration) {
		Iterable<Video> it1 = videosRepository.findByDurationLessThan(duration);
		System.out.println("findByDurationLessThan:"+it1);
		return Lists.newArrayList(it1);
	}

	@RequestMapping(value = VIDEO_SVC_LIKE_VIDEO, method = RequestMethod.POST)
	public @ResponseBody Video likeVideo(@PathVariable("id") long id, Principal p, HttpServletResponse response) {
		Video v = videosRepository.findOne(id);
		if(v != null) {
			System.out.println("Video Exists");
			Set<String> userNameSet = v.getLikesUsernames();
			if(userNameSet.contains(p.getName())) {
				System.out.println("UserName exist. Cannot like again");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				
			}
			else {
				userNameSet.add(p.getName());
				v.setLikesUsernames(userNameSet);
				v.setLikes(userNameSet.size());
				videosRepository.save(v);
			}
		}
		else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
		return v;
	}

	@RequestMapping(value = VIDEO_SVC_UNLIKE_VIDEO, method = RequestMethod.POST)
	public @ResponseBody Video unlikeVideo(@PathVariable("id") long id, Principal p, HttpServletResponse response) {
		Video v = videosRepository.findOne(id);
		if(v != null) {
			System.out.println("Video Exists");
			Set<String> userNameSet = v.getLikesUsernames();
			if(userNameSet.contains(p.getName())) {
				System.out.println("Unlike the video");
				userNameSet.remove(p.getName());
				v.setLikesUsernames(userNameSet);
				v.setLikes(userNameSet.size());
				videosRepository.save(v);
				
			}
			else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		}
		else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
		return v;
	}



	@RequestMapping(value = VIDEO_SVC_PATH + "/{id}/likedby", method = RequestMethod.GET)
	public @ResponseBody Collection<String> getUsersWhoLikedVideo(@PathVariable("id") long id) {

		Video v = videosRepository.findOne(id);
		if(v != null) {
			return v.getLikesUsernames();
		}
		else {
			
			throw new ResourceNotFoundException();
		}
	}

}
