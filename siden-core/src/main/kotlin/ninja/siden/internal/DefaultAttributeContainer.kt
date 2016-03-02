package ninja.siden.internal

import ninja.siden.AttributeContainer
import java.util.*

/**
 * @author taichi
 */

class DefaultAttributeContainer : AttributeContainer {
    val attrs = HashMap<String, Any>()

    @Suppress("UNCHECKED_CAST")
    override fun <T> attr(key: String, newone: T): Optional<T> {
        return Optional.ofNullable(this.attrs.put(key, newone as Any) as T)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> attr(key: String): Optional<T> {
        return Optional.ofNullable(this.attrs[key] as T)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> remove(key: String): Optional<T> {
        return Optional.ofNullable(this.attrs.remove(key) as T)
    }

    @Suppress("UNCHECKED_CAST")
    override fun iterator(): Iterator<AttributeContainer.Attr> {
        val itr = this.attrs.entries.iterator()
        return object : Iterator<AttributeContainer.Attr> {
            override fun hasNext(): Boolean = itr.hasNext()

            override fun next(): AttributeContainer.Attr {
                val ent = itr.next()
                return object : AttributeContainer.Attr {
                    override val name: String
                        get() = ent.key

                    override fun <T> value(): T = ent.value as T

                    override fun <T> remove(): T {
                        itr.remove()
                        return ent.value as T
                    }
                }
            }
        }
    }


}