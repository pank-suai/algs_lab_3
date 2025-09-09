import kotlin.random.Random
import kotlin.random.nextUInt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Статическая последовательность
 */
interface StaticSequence<T> {
    /**
     * Размер
     */
    val size: Int

    /**
     * Пустая ли последовательность
     */
    val isEmpty: Boolean

    /**
     * Можно ли добавить новый элемент (Есть ли место?)
     */
    val canAdd: Boolean

    /**
     * Добавление элемента
     */
    fun push(item: T)

    /**
     * Удаление элемента
     */
    fun pop(): T

}

/**
 * Статический стэк
 *
 * @param size размер массива под стек
 */
class Stack<T>(override val size: Int) : StaticSequence<T> {
    private val _stackArray: Array<T?> = arrayOfNulls(size)

    // Текущий индекс
    private var _index = -1
    override val isEmpty
        get() = _index < 0
    override val canAdd: Boolean
        get() = _index + 1 < size

    override fun pop(): T {
        require(!isEmpty) { "Стек пуст" }
        val item = _stackArray[_index]
        _stackArray[_index--] = null
        requireNotNull(item)
        return item
    }

    override fun push(item: T) {
        require(canAdd) { "Стек заполнен" }
        _stackArray[++_index] = item
    }

    override fun toString(): String {
        return buildString {
            append("Стек[")
            for (i in _index downTo 0) {
                append(_stackArray[i])
                if (i != 0) append(", ")
            }
            append("]")
        }
    }
}

/**
 * Статическая очередь
 */
class Queue<T>(override val size: Int) : StaticSequence<T> {
    private val _queueArray: Array<T?> = arrayOfNulls(size)
    private var _isEmpty = true
    override val isEmpty: Boolean
        get() = _isEmpty
    override val canAdd: Boolean
        get() = isEmpty || _startIndex != ((_endIndex + 1) % size) // Дубликация кода :(
    private var _startIndex = -1
    private var _endIndex = -1

    override fun push(item: T) {
        require(canAdd) { "Стек заполнен" }
        val newIndex = (_endIndex + 1) % size
        _endIndex = newIndex
        _queueArray[newIndex] = item
        if (_isEmpty)
            _isEmpty = !isEmpty
    }

    override fun pop(): T {
        require(!isEmpty) { "Очередь пуста" }
        val newIndex = (_startIndex + 1) % size
        val item = _queueArray[newIndex]
        requireNotNull(item)
        _queueArray[newIndex] = null
        _startIndex = newIndex
        if (_startIndex == _endIndex) {
            _isEmpty = true
        }
        return item
    }

    override fun toString(): String {
        return buildString {
            append("Очередь[")
            append(_queueArray.filterNotNull().joinToString(", ")) // Мне очень лень делать нормальный вывод в консоль
            append("]")
        }
    }


}


/**
 * Задача
 *
 * @param id идентификатор/название задачи
 * @param tacts количество тактов на выполнение
 * @param currentTact текущий такт
 */
@OptIn(ExperimentalUuidApi::class)
data class Task(val id: String, val tacts: UInt) {
    companion object {


        /**
         * Генерация задач
         *
         * @param n количество задач
         */
        fun generate(n: UInt): List<Task> {
            return buildList {
                repeat(n.toInt()) {
                    add(Task(Uuid.random().toHexString(), Random.nextUInt(1u, n)))
                }
            }
        }
    }
}

class Processor(val name: String) {
    private var _task: Task? = null
    private var taskTacts = 0u

    var task: Task?
        get() = _task
        set(value) {
            taskTacts = 0u
            _task = value
        }

    val isBusy: Boolean
        get() = task != null
    val isTaskComplete: Boolean
        get() = isBusy && task?.tacts == taskTacts

    fun tact() {
        if (task == null) {
            println("Процессор $name свободен")
            return
        }
        println("Процессор $name выполняет $task ${taskTacts++}/${task?.tacts}")

    }
}


@OptIn(ExperimentalUuidApi::class)
fun main() {
    println("Ты хочешь (1) сгенерировать задачи или (2) ввести сам? (Введи число)")
    var genTask: Boolean? = null
    while (genTask == null) {
        genTask = when (readln()) {
            "1" -> true
            "2" -> false
            else -> null
        }
    }
    val tasks: MutableList<Task>
    if (genTask) {
        val n = readUInt("Введите количество задач, которое вы хотите сгенерировать")
        tasks = Task.generate(n).toMutableList()
    } else {
        val n = readUInt("Введите количество задач, которое вы хотите ввести")
        tasks = buildList {
            repeat(n.toInt()) {
                println("Введите ID задачи (по умолчанию сгенерируется UUID)")

                val id = readln().ifEmpty { Uuid.random().toHexString() }
                val tacts = readUInt("Введите количество тактов на выполнение задачи")
                add(Task(id, tacts))
            }
        }.toMutableList()
    }
    println(tasks)

    val stackSize =
        readUInt(
            "Введите максимальное количество элементов в стеке (по умолчанию: количество созданных задач)",
            tasks.size.toUInt()
        )
    val queueSize =
        readUInt(
            "Введите максимальное количество элементов в очереди (по умолчанию: количество созданных задач)",
            tasks.size.toUInt()
        )

    var seqMode: Boolean? = null
    while (seqMode == null) {
        println("Включить последовательный режим вывода по тактам? (1 - да, 0 нет)")
        seqMode = when (readln()) {
            "0" -> false
            "1" -> true
            else -> null
        }
    }

    var tact = 0
    val stack = Stack<Task>(stackSize.toInt())
    val queue = Queue<Task>(queueSize.toInt())
    val p1 = Processor("P1")
    val p2 = Processor("P2")

    while (tasks.isNotEmpty() || !stack.isEmpty || p1.isBusy || !queue.isEmpty || p2.isBusy) {
        println("Такт $tact")
        tact++

        /* Главное чтобы никто не выхватывал элемент как только его положили */
        if (!p2.isBusy && !queue.isEmpty) {
            p2.task = queue.pop()
        }
        if (!p1.isBusy && !stack.isEmpty) {
            p1.task = stack.pop()
        }
        if (p2.isTaskComplete) {
            val task = p2.task
            p2.task = null
            println("Задача выполнена: $task")
        }
        if (p1.isTaskComplete) {
            val task = p1.task!!
            try {
                queue.push(task)
                p1.task = null
            }catch (e: Exception){
                println("Ошибка при добавлении в очередь: ${e.message}")
            }
        }



        if (tasks.isNotEmpty()) {
            val task = tasks.removeFirst()

            try {
                stack.push(task)
            } catch (e: Exception) {
                println("Ошибка при добавлении в стек: ${e.message}")
                tasks.addFirst(task)
            }
        }


        // Вывод
        println(tasks)
        println(stack)
        p1.tact()
        println(queue)
        p2.tact()
        if (seqMode) {
            readln()
        } else {
            println()
        }
    }
    println("Работа завершена")
}


fun readUInt(question: String, defaultValue: UInt? = null): UInt {
    var n: UInt? = null
    while (n == null) {
        println(question)
        n = readln().toUIntOrNull() ?: defaultValue
    }
    return n
}