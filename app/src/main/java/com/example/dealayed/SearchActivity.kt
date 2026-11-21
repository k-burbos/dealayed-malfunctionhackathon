package com.example.dealayed

import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

// ---------- data ----------
data class SearchDeal(
    val category: String,          // Food, Movies, Travel, Shopping, Others
    val title: String,
    val description: String,
    val nowPrice: Int,
    val missedPrice: Int,
    val imageUrl: String? = null   // backend fills this in later
)

// ---------- adapter ----------
class SearchDealAdapter : ListAdapter<SearchDeal, SearchDealAdapter.VH>(Diff) {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.ivDeal)
        val category: TextView = view.findViewById(R.id.tvCategory)
        val title: TextView = view.findViewById(R.id.tvDealTitle)
        val desc: TextView = view.findViewById(R.id.tvDescription)
        val now: TextView = view.findViewById(R.id.tvNow)
        val missed: TextView = view.findViewById(R.id.tvMissed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_search_deal, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val d = getItem(position)
        holder.category.text = d.category
        holder.title.text = d.title
        holder.desc.text = d.description
        holder.now.text = "₱%,d".format(d.nowPrice)
        holder.missed.text = "₱%,d".format(d.missedPrice)
        holder.missed.paintFlags = holder.missed.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        // TODO (backend): load d.imageUrl into holder.image
    }

    private object Diff : DiffUtil.ItemCallback<SearchDeal>() {
        override fun areItemsTheSame(a: SearchDeal, b: SearchDeal) = a.title == b.title
        override fun areContentsTheSame(a: SearchDeal, b: SearchDeal) = a == b
    }
}

// ---------- screen ----------
class SearchActivity : AppCompatActivity() {

    private val categories = listOf("Food", "Movies", "Travel", "Shopping", "Others")

    private val allDeals = listOf(
        SearchDeal("Shopping", "Running shoes sale", "Buy 1, get 1 at 50% off", 2400, 1200),
        SearchDeal("Food", "Lunch Combo Rush", "Burger, fries & drink for ₱199", 299, 199),
        SearchDeal("Travel", "Boracay Weekend Fare", "Round trip from ₱1,999", 3500, 1999),
        SearchDeal("Food", "Coffee Buy 2 Take 1", "Third cup free for members", 2400, 1800),
        SearchDeal("Movies", "Cinema Date Night", "2 tickets + popcorn combo", 600, 450),
        SearchDeal("Food", "Pizza Party Pack", "Large pizza + 2 drinks", 650, 499),
        SearchDeal("Shopping", "Weekend Tote Sale", "Buy 2, get 1 free", 900, 600),
        SearchDeal("Travel", "Cebu Day Tour", "Island hopping for two", 2800, 1900),
        SearchDeal("Movies", "Marathon Pass", "3 movies, 1 price", 450, 300)
    )

    private val adapter = SearchDealAdapter()
    private var selectedCategory: String? = null   // null = show everything

    private lateinit var dropdownPanel: LinearLayout
    private lateinit var tvSelectedCategory: TextView
    private lateinit var ivArrow: ImageView
    private lateinit var recHeader: View
    private lateinit var tvEmptyInfo: TextView
    private lateinit var emptyEmoji: TextView
    private lateinit var rvDeals: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        // The shared nav bar is now a plain View, not a BottomNavigationView
        val bottomNav = findViewById<View>(R.id.bottomNav)
        val statusSpace = findViewById<Space>(R.id.statusBarSpace)
        val navPad = (8 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, 0, bars.right, 0)
            statusSpace.layoutParams = statusSpace.layoutParams.apply { height = bars.top }
            bottomNav.setPadding(0, navPad, 0, navPad + bars.bottom)
            insets
        }

        // Styles the tabs and wires up Home / Search / Profile taps
        BottomNav.setup(this, BottomNav.Tab.SEARCH)

        dropdownPanel = findViewById(R.id.dropdownPanel)
        tvSelectedCategory = findViewById(R.id.tvSelectedCategory)
        ivArrow = findViewById(R.id.ivArrow)
        recHeader = findViewById(R.id.recHeader)
        tvEmptyInfo = findViewById(R.id.tvEmptyInfo)
        emptyEmoji = findViewById(R.id.emptyEmoji)
        rvDeals = findViewById(R.id.rvDeals)

        rvDeals.layoutManager = LinearLayoutManager(this)
        rvDeals.adapter = adapter
        rvDeals.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0) setDropdownOpen(false)
            }
        })

        buildDropdownItems()
        findViewById<View>(R.id.categorySelector).setOnClickListener {
            setDropdownOpen(dropdownPanel.visibility != View.VISIBLE)
        }

        applyFilter()
    }

    private fun buildDropdownItems() {
        categories.forEach { cat ->
            val item = TextView(this).apply {
                text = cat
                textSize = 15f
                setTextColor(0xFF222222.toInt())
                gravity = Gravity.CENTER_VERTICAL
                minHeight = (38 * resources.displayMetrics.density).toInt()
                setOnClickListener { onCategoryPicked(cat) }
            }
            dropdownPanel.addView(item)
        }
    }

    private fun onCategoryPicked(cat: String) {
        // tapping the already-selected category clears the filter
        selectedCategory = if (selectedCategory == cat) null else cat
        tvSelectedCategory.text = selectedCategory ?: "Category"
        setDropdownOpen(false)
        applyFilter()
    }

    private fun setDropdownOpen(open: Boolean) {
        dropdownPanel.visibility = if (open) View.VISIBLE else View.GONE
        ivArrow.setImageResource(if (open) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down)
    }

    private fun applyFilter() {
        val cat = selectedCategory
        val filtered = when {
            cat == null -> allDeals
            cat == "Others" -> allDeals.filter { d ->
                categories.dropLast(1).none { it.equals(d.category, ignoreCase = true) }
            }
            else -> allDeals.filter { it.category.equals(cat, ignoreCase = true) }
        }
        adapter.submitList(filtered)

        val empty = filtered.isEmpty()
        recHeader.visibility = if (empty) View.GONE else View.VISIBLE
        rvDeals.visibility = if (empty) View.INVISIBLE else View.VISIBLE
        tvEmptyInfo.visibility = if (empty) View.VISIBLE else View.GONE
        emptyEmoji.visibility = if (empty) View.VISIBLE else View.GONE
    }
}