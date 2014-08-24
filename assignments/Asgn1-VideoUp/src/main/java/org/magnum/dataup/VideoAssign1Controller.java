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
package org.magnum.dataup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class VideoAssign1Controller {

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
	
	private HashMap<Long, Video> mapOfVideos = new HashMap<Long, Video>();
	
	public static final String VIDEO_SVC_PATH = "/video";
	
	public static final String VIDEO_SVC_ID_PATH = "/video/{id}/data";
	

	
	private static final AtomicLong currentId = new AtomicLong(0L);
	

	@RequestMapping(value = VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {

		//Read the number of files from the set
		Set<Long> numberOfElements = mapOfVideos.keySet();
		List<Video> listOfVideos = new ArrayList<Video>();
		Iterator<Long> it = numberOfElements.iterator();
		while(it.hasNext()) {
			
			Video v = mapOfVideos.get(it.next());
			listOfVideos.add(v);
			
		}
		
		return listOfVideos;
	}

	@RequestMapping(value = VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideoMetaDat(@RequestBody Video v) throws IOException {
        checkAndSetId(v);
		v.setDataUrl(getDataUrl(v.getId()).trim());
		mapOfVideos.put(v.getId(), v);
		return v;
	}
	

	@RequestMapping(value = VIDEO_SVC_ID_PATH, method = RequestMethod.POST)
	public @ResponseBody VideoStatus addVideoData(@PathVariable("id") long id, @RequestParam("data") MultipartFile m, HttpServletResponse response) throws IOException {

		Video v = null;
		VideoStatus vs = null;
		if(mapOfVideos.get(id) != null)
		{
			v = mapOfVideos.get(id);
			v.setId(v.getId());
			VideoFileManager vFileMgr = VideoFileManager.get();
			vFileMgr.saveVideoData(v, m.getInputStream());
			vs = new VideoStatus(VideoState.READY);
			
		}
		else {
			response.setStatus(404);
			
		}
		return vs;
	}
	
	

	
	@RequestMapping(value = VIDEO_SVC_ID_PATH, method = RequestMethod.GET)
	public void getVideoData(@PathVariable("id") long id, HttpServletResponse response) throws IOException {
		
		Video v = null;
		VideoFileManager vFileMgr = VideoFileManager.get();
		if(mapOfVideos.get(id) != null) {
			v = mapOfVideos.get(id);
			vFileMgr.copyVideoData(v, response.getOutputStream());
		}
		else {
			response.setStatus(404);
		}
		
		
		
	}

	
    private void checkAndSetId(Video entity) {
        if(entity.getId() == 0){
            entity.setId(currentId.incrementAndGet());
        }
    }	

	
    private String getDataUrl(long videoId) {
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }

    private String getUrlBaseForLocalServer() {
       HttpServletRequest request = 
           ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
       String base = 
          "http://"+request.getServerName() 
          + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
       return base;
    }

}
