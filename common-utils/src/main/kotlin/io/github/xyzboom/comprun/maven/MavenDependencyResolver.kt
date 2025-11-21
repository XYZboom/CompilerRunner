package io.github.xyzboom.comprun.maven

import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.resolution.DependencyResolutionException
import org.eclipse.aether.supplier.RepositorySystemSupplier
import java.io.File
import kotlin.jvm.Throws

class MavenDependencyResolver(
    private val localRepository: LocalRepository = defaultLocalRepository,
    private val remoteRepositories: List<RemoteRepository> = listOf(mavenCentralRepository, jcenterRepository)
) {
    companion object {
        val defaultLocalRepository = LocalRepository("${System.getProperty("user.home")}/.m2/repository")
        val mavenCentralRepository: RemoteRepository =
            RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build()
        val jcenterRepository: RemoteRepository =
            RemoteRepository.Builder("jcenter", "default", "https://jcenter.bintray.com/").build()
    }

    private val repositorySystem: RepositorySystem = RepositorySystemSupplier().get()
    private val session: RepositorySystemSession = MavenRepositorySystemUtils.newSession().also {
        it.localRepositoryManager = repositorySystem.newLocalRepositoryManager(it, localRepository)
    }

    /**
     * Resolve artifact file. @see [DefaultArtifact].
     */
    @Throws(DependencyResolutionException::class)
    fun resolve(artifact: Artifact): File {
        val request = ArtifactRequest(artifact, remoteRepositories, null)
        val result = repositorySystem.resolveArtifact(session, request)
        return result.artifact.file
    }

    /**
     * Resolve artifact and dependencies. @see [DefaultArtifact].
     */
    @Throws(DependencyResolutionException::class)
    fun resolveWithDependencies(artifact: Artifact): List<File> {
        val dependency = Dependency(artifact, "compile")

        val collectRequest = CollectRequest(dependency, remoteRepositories)
        val dependencyRequest = DependencyRequest(collectRequest, null)

        val result = repositorySystem.resolveDependencies(session, dependencyRequest)
        return result.artifactResults.map { it.artifact.file }
    }
}
