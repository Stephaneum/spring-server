package de.stephaneum.spring.helper

import de.stephaneum.spring.database.*
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class PostService (
        private val postRepo: PostRepo,
        private val filePostRepo: FilePostRepo
) {

    fun getPosts(menu: Int, noContent: Boolean = false, pageable: Pageable? = null): List<Post> {
        if (pageable != null)
            return digest(postRepo.findByMenuIdOrderByTimestampDesc(menu, pageable), noContent)
        else
            return digest(postRepo.findByMenuIdOrderByTimestampDesc(menu), noContent)
    }

    fun getUnapprovedPosts(user: User, noContent: Boolean = false): List<Post> {
        return digest(postRepo.findUnapproved(user.id), noContent)
    }

    fun getAllUnapprovedPosts(noContent: Boolean = false): List<Post> {
        return digest(postRepo.findUnapproved(), noContent)
    }

    private fun digest(posts: List<Post>, noContent: Boolean): List<Post> {
        if(posts.isEmpty())
            return emptyList()

        val images = filePostRepo.findImagesByPostIdIn(posts.map { it.id })
        return posts.onEach { p ->
            p.simplify()
            p.menu?.simplify()
            p.images = images
                    .filter { it.postID == p.id }
                    .map { it.file.apply { simplifyForPosts() } }
            if(noContent)
                p.content = null
        }
    }

}