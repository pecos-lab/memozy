import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.killsunghun.Memo

class MemoViewModel : ViewModel() {

    var memoList = mutableStateListOf<Memo>()
        private set


    // add update delete = 비즈니스 모델
    fun addMemo(memo: Memo) {
        memoList.add(memo)
    }

    fun updateMemo(index: Int, memo: Memo) {
        memoList[index] = memo
    }

    fun deleteMemo(index: Int) {
        memoList.removeAt(index)
    }
}