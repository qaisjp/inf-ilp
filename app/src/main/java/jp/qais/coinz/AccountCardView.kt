package jp.qais.coinz

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.TextView

/**
 * TODO: document your custom view class.
 */
class AccountCardView : CardView {
//    private var textPaint: TextPaint? = null
//    private var textWidth: Float = 0f
//    private var textHeight: Float = 0f

    private var _isBank: Boolean = false
    private var _balance: Float = -1f
    private var _currency: Currency = Currency.GOLD

    /**
     * Whether or not this Card refers to a Bank Account or Wallet
     */
    var isBank: Boolean
        get() = _isBank
        set(value) {
            _isBank = value
            invalidateTextPaintAndMeasurements()
        }

    /**
     * Set the balance of this card
     */
    var balance: Float
        get() = _balance
        set(value) {
            _balance = value
            invalidateTextPaintAndMeasurements()
        }

    /**
     * The currency of this card
     */
    var currency: Currency
        get() = _currency
        set(value) {
            _currency = value
            invalidateTextPaintAndMeasurements()
        }

    /**
     * In the example view, this drawable is drawn above the text.
     */
    var exampleDrawable: Drawable? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.AccountCardView, defStyle, 0)

        _isBank = a.getBoolean(R.styleable.AccountCardView_isBank, false)
        _balance = a.getFloat(R.styleable.AccountCardView_balance, -1f)
        _currency = Currency.values().get(a.getInt(R.styleable.AccountCardView_currency, 0))

//        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
//        // values that should fall on pixel boundaries.
//        _exampleDimension = a.getDimension(
//                R.styleable.AccountCardView_exampleDimension,
//                exampleDimension)
//
//        if (a.hasValue(R.styleable.AccountCardView_exampleDrawable)) {
//            exampleDrawable = a.getDrawable(
//                    R.styleable.AccountCardView_exampleDrawable)
//            exampleDrawable?.callback = this
//        }
//
        a.recycle()
//
//        // Set up a default TextPaint object
//        textPaint = TextPaint().apply {
//            flags = Paint.ANTI_ALIAS_FLAG
//            textAlign = Paint.Align.LEFT
//        }
//

        View.inflate(context, R.layout.card_account, this)

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements()
    }

    private fun invalidateTextPaintAndMeasurements() {
//        textPaint?.let {
//            it.textSize = exampleDimension
//            it.color = exampleColor
//            textWidth = it.measureText(exampleString)
//            textHeight = it.fontMetrics.bottom
//        }

        findViewById<TextView>(R.id.text_balance).text = String.format("%.05f", balance)
        findViewById<TextView>(R.id.text_balance_description).text = if (isBank) {
            context.getText(R.string.available_balance)
        } else {
            context.getText(R.string.in_wallet)
        }
        findViewById<TextView>(R.id.text_currency).text = currency.getString(context)
    }
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//
//        // TODO: consider storing these as member variables to reduce
//        // allocations per draw cycle.
//        val paddingLeft = paddingLeft
//        val paddingTop = paddingTop
//        val paddingRight = paddingRight
//        val paddingBottom = paddingBottom
//
//        val contentWidth = width - paddingLeft - paddingRight
//        val contentHeight = height - paddingTop - paddingBottom
//
//        exampleString?.let {
//            // Draw the text.
//            canvas.drawText(it,
//                    paddingLeft + (contentWidth - textWidth) / 2,
//                    paddingTop + (contentHeight + textHeight) / 2,
//                    textPaint)
//        }
//
//        // Draw the example drawable on top of the text.
//        exampleDrawable?.let {
//            it.setBounds(paddingLeft, paddingTop,
//                    paddingLeft + contentWidth, paddingTop + contentHeight)
//            it.draw(canvas)
//        }
//    }
}
