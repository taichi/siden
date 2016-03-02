package ninja.siden

// see. https://youtrack.jetbrains.com/issue/KT-5899
inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R {
    var closed = false
    try {
        return block(this)
    } catch(e: java.lang.Exception) {
      closed = true
        try {
            this.close()
        } catch(ce: Exception) {
            //e.addSuppressed(ce)
        }
        throw e
    } finally {
        if(closed == false) {
            this.close()
        }
    }
}