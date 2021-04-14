package m.tech.demoexopool.hnim_exo

object HnimExoUtils {

    fun executeTimeInMillis(action:() -> Unit): Long{
        val startTime = System.currentTimeMillis()
        action()
        return System.currentTimeMillis() - startTime
    }

}