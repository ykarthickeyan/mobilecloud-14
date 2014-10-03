package org.magnum.mobilecloud.video.repository;

import java.util.Collection;

import org.springframework.data.repository.CrudRepository;

/**
 * An interface for a repository that can store Video objects and allow them to
 * be searched by title.
 * 
 * @author karthickeyan
 * 
 */
public interface VideoRepository extends CrudRepository<Video, Long> {

	// Find all videos with a matching title (e.g., Video.name)
	public Collection<Video> findByName(String title);
	
	
	public Collection<Video> findByDurationLessThan(long duration);
}
