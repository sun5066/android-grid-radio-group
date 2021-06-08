import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.gridlayout.widget.GridLayout


/**
 * RadioGroup  LinearLayout -> GridLayout 으로 새로 탄생한 View
 *
 * @attr rowCount="2"   : 행 수
 * @attr columnCount="2": 열 수
 *
 * @author sun5066
 * @since 21.06.08
 * */
class GridRadioGroup : GridLayout, ViewGroup.OnHierarchyChangeListener {

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { init() }

    private val NOTHING_CHECKED = -1

    private var mCheckedCheckableImageButtonId = NOTHING_CHECKED
    private var mChildOnCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null
    private var mProtectFromCheckedChange = false
    private var mOnCheckedChangeListener: OnCheckedChangeListener? = null


    private fun init() {
        mChildOnCheckedChangeListener = CheckedStateTracker()
        super.setOnHierarchyChangeListener(this)
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        mOnCheckedChangeListener = listener
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (mCheckedCheckableImageButtonId != NOTHING_CHECKED) {
            mProtectFromCheckedChange = true
            setCheckedStateForView(mCheckedCheckableImageButtonId, true)

            mProtectFromCheckedChange = false
            setCheckedId(mCheckedCheckableImageButtonId)
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child is AppCompatRadioButton) {
            if (child.isChecked) {
                mProtectFromCheckedChange = true
                mCheckedCheckableImageButtonId.takeIf { it != NOTHING_CHECKED }?.let { setCheckedStateForView(it, false) }

                mProtectFromCheckedChange = false
                setCheckedId(child.id)
            }
        }
        super.addView(child, index, params)
    }


    /* ViewGroup.OnHierarchyChangeListener */
    override fun onChildViewAdded(parent: View?, child: View?) {
        if (parent === this@GridRadioGroup && child is AppCompatRadioButton) {
            val id = child.getId()
            if (id == NO_ID) child.setId(generateViewId())

            child.setOnCheckedChangeListener(mChildOnCheckedChangeListener)
        }
    }

    override fun onChildViewRemoved(parent: View?, child: View?) {
        if (parent === this@GridRadioGroup && child is AppCompatRadioButton) child.setOnCheckedChangeListener(null)
    }


    fun clearCheck() {
        check(NOTHING_CHECKED)
    }

    private fun check(id: Int) {
        if (id != NOTHING_CHECKED && id == mCheckedCheckableImageButtonId) return
        if (mCheckedCheckableImageButtonId != NOTHING_CHECKED) setCheckedStateForView(mCheckedCheckableImageButtonId, false)
        if (id != NOTHING_CHECKED) setCheckedStateForView(id, true)

        setCheckedId(id)
    }

    private fun setCheckedId(id: Int) {
        mCheckedCheckableImageButtonId = id
        mOnCheckedChangeListener?.onCheckedChanged(this, mCheckedCheckableImageButtonId)
    }

    private fun setCheckedStateForView(viewId: Int, checked: Boolean) {
        val checkedView = findViewById<View>(viewId)
        if (checkedView != null && checkedView is AppCompatRadioButton) checkedView.isChecked = checked
    }


    /* checkChange 리스너 */
    interface OnCheckedChangeListener {
        fun onCheckedChanged(radioGroup: GridRadioGroup?, checkedId: Int)
    }


    private inner class CheckedStateTracker : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            if (mProtectFromCheckedChange) return

            mProtectFromCheckedChange = true
            if (mCheckedCheckableImageButtonId != NOTHING_CHECKED)
                setCheckedStateForView(mCheckedCheckableImageButtonId, false)

            mProtectFromCheckedChange = false
            val id = buttonView.id
            setCheckedId(id)
        }
    }
}
