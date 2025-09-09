package kr.cjs.catty.viewmodel

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import kotlin.random.Random


class CounterViewModel : ViewModel() {
    private val _count1 = mutableIntStateOf(0)
    val count1: MutableIntState = _count1

    private val _count2 = mutableIntStateOf(0)
    val count2: MutableIntState = _count2

    fun incrementCount1() {
        _count1.value++
    }

    fun decrementCount1() {
        if (_count1.value > 0) {
            _count1.value--
        }
    }

    fun incrementCount2() {
        _count2.value++
    }

    fun decrementCount2() {
        if (_count2.value > 0) {
            _count2.value--
        }
    }
}


class LottoViewModel : ViewModel(){
    private val _numbers = mutableStateListOf<Int>()
    val numbers: SnapshotStateList<Int> = _numbers

    init {
        generate()
    }

    fun generate() {
        val newNumbers = (1..45).shuffled().take(6).sorted()
        _numbers.clear()
        _numbers.addAll(newNumbers)
    }
}



class LottoViewModel2 : ViewModel() {

    private var _numbers = IntArray(6) { 0 }
    val numbers
        get() = _numbers

    fun generate(){
        for (i in _numbers.indices) {
            _numbers[i] = Random.nextInt(1, 46)
        }
    }

}


data class Song(var title: String, var singer: String)

class SongViewModel : ViewModel() {
    private val _songs = mutableStateListOf<Song>()
    val songs: List<Song>
        get() = _songs

    fun add(song: Song) {
        _songs.add(song)
    }
}