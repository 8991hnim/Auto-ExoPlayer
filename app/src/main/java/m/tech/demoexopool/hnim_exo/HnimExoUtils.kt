package m.tech.demoexopool.hnim_exo

/**
 * @author: 89hnim
 * @since: 13/04/2021
 */
object HnimExoUtils {

    fun executeTimeInMillis(action:() -> Unit): Long{
        val startTime = System.currentTimeMillis()
        action()
        return System.currentTimeMillis() - startTime
    }

}