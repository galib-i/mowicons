package app.galib.mowicons.data.repository

import app.galib.mowicons.data.api.GitHubContributorsAPI
import app.galib.mowicons.ui.destination.about.coreContributorIds
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
class GitHubContributorsRepository(
    private val api: GitHubContributorsAPI,
    private val contributorIds: List<Long> = coreContributorIds,
) {
    suspend fun getTopContributors() = api.getContributors()
        .filterNot { contributorIds.contains(it.id) }
        .sortedByDescending { it.contributions }
}
