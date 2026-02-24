package com.gorguludg.tictactoe

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val cells = Array(3) { Array<TextView?>(3) { null } }
    private lateinit var btnRestart: TextView
    private lateinit var textStatus: TextView
    private lateinit var textScoreX: TextView
    private lateinit var textScoreO: TextView
    private lateinit var textVs: TextView

    // Layouts for theme changes
    private lateinit var mainLayout: LinearLayout
    private lateinit var headerLayout: RelativeLayout
    private lateinit var modeToggleContainer: LinearLayout
    private lateinit var scoreCardX: LinearLayout
    private lateinit var scoreCardO: LinearLayout

    // Mode toggle
    private lateinit var btnPvP: TextView
    private lateinit var btnPvC: TextView

    // Symbol toggle
    private lateinit var symbolToggle: LinearLayout
    private lateinit var btnChooseX: TextView
    private lateinit var btnChooseO: TextView

    // Theme toggle
    private lateinit var themeToggle: LinearLayout
    private lateinit var themeIcon: TextView

    private lateinit var gameLogic: GameLogic
    private var computerAI: ComputerAI? = null

    // Game settings
    private var vsComputer = false
    private var playerSymbol = "X"
    private var computerSymbol = "O"
    private var isDarkTheme = false

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load saved theme preference
        val prefs = getSharedPreferences("TicTacToePrefs", Context.MODE_PRIVATE)
        isDarkTheme = prefs.getBoolean("isDarkTheme", false)

        gameLogic = GameLogic()
        initializeUI()
        applyTheme()
        updateTurnDisplay()
    }

    private fun initializeUI() {
        // Layouts
        mainLayout = findViewById(R.id.mainLayout)
        headerLayout = findViewById(R.id.headerLayout)
        modeToggleContainer = findViewById(R.id.modeToggleContainer)
        scoreCardX = findViewById(R.id.scoreCardX)
        scoreCardO = findViewById(R.id.scoreCardO)

        // Text views
        textStatus = findViewById(R.id.textStatus)
        textScoreX = findViewById(R.id.textScoreX)
        textScoreO = findViewById(R.id.textScoreO)
        textVs = findViewById(R.id.textVs)
        btnRestart = findViewById(R.id.btnRestart)

        // Mode toggle
        btnPvP = findViewById(R.id.btnPvP)
        btnPvC = findViewById(R.id.btnPvC)

        // Symbol toggle
        symbolToggle = findViewById(R.id.symbolToggle)
        btnChooseX = findViewById(R.id.btnChooseX)
        btnChooseO = findViewById(R.id.btnChooseO)

        // Theme toggle
        themeToggle = findViewById(R.id.themeToggle)
        themeIcon = findViewById(R.id.themeIcon)

        // Game cells
        cells[0][0] = findViewById(R.id.btn00)
        cells[0][1] = findViewById(R.id.btn01)
        cells[0][2] = findViewById(R.id.btn02)
        cells[1][0] = findViewById(R.id.btn10)
        cells[1][1] = findViewById(R.id.btn11)
        cells[1][2] = findViewById(R.id.btn12)
        cells[2][0] = findViewById(R.id.btn20)
        cells[2][1] = findViewById(R.id.btn21)
        cells[2][2] = findViewById(R.id.btn22)

        // Cell click listeners
        for (i in 0..2) {
            for (j in 0..2) {
                val row = i
                val col = j
                cells[i][j]?.setOnClickListener {
                    onCellClicked(row, col)
                }
            }
        }

        // Mode toggle listeners
        btnPvP.setOnClickListener {
            setModeToggle(true)
            vsComputer = false
            symbolToggle.visibility = View.GONE
            resetScores()
            restartGame()
        }

        btnPvC.setOnClickListener {
            setModeToggle(false)
            vsComputer = true
            symbolToggle.visibility = View.VISIBLE
            computerAI = ComputerAI(computerSymbol, playerSymbol)
            resetScores()
            restartGame()
        }

        // Symbol toggle listeners
        btnChooseX.setOnClickListener {
            setSymbolToggle(true)
            playerSymbol = "X"
            computerSymbol = "O"
            computerAI = ComputerAI(computerSymbol, playerSymbol)
            resetScores()
            restartGame()
        }

        btnChooseO.setOnClickListener {
            setSymbolToggle(false)
            playerSymbol = "O"
            computerSymbol = "X"
            computerAI = ComputerAI(computerSymbol, playerSymbol)
            resetScores()
            restartGame()
        }

        // Theme toggle listener
        themeToggle.setOnClickListener {
            toggleTheme()
        }

        // Restart button
        btnRestart.setOnClickListener {
            restartGame()
        }
    }

    private fun toggleTheme() {
        isDarkTheme = !isDarkTheme

        // Save preference
        val prefs = getSharedPreferences("TicTacToePrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("isDarkTheme", isDarkTheme).apply()

        // Animate theme icon
        animateThemeToggle()

        // Apply theme
        applyTheme()
    }

    private fun animateThemeToggle() {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 300
        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Float
            val translationX = if (isDarkTheme) {
                progress * (themeToggle.width - themeIcon.width - 8)
            } else {
                (1 - progress) * (themeToggle.width - themeIcon.width - 8)
            }
            themeIcon.translationX = translationX
        }
        animator.start()

        // Update icon with slight delay for smooth transition
        handler.postDelayed({
            themeIcon.text = if (isDarkTheme) "🌙" else "☀️"
        }, 150)
    }

    private fun applyTheme() {
        if (isDarkTheme) {
            applyDarkTheme()
        } else {
            applyLightTheme()
        }

        // Reapply toggle states
        setModeToggle(!vsComputer)
        if (vsComputer) {
            setSymbolToggle(playerSymbol == "X")
        }
    }

    private fun applyLightTheme() {
        // Background
        mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.light_background))

        // Header stays yellow
        headerLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))

        // Mode toggle
        modeToggleContainer.setBackgroundResource(R.drawable.toggle_background)
        symbolToggle.setBackgroundResource(R.drawable.toggle_background)

        // Scoreboard
        scoreCardX.setBackgroundResource(R.drawable.score_card_x)
        scoreCardO.setBackgroundResource(R.drawable.score_card_o)
        textVs.setTextColor(ContextCompat.getColor(this, R.color.light_text_secondary))

        // Status text
        textStatus.setTextColor(ContextCompat.getColor(this, R.color.light_text))

        // Cells - check if it's a winning cell or regular cell
        val winningCells = gameLogic.getWinningCells()
        for (i in 0..2) {
            for (j in 0..2) {
                cells[i][j]?.apply {
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.light_text))

                    // Check if this is a winning cell
                    val isWinningCell = winningCells?.any { it.first == i && it.second == j } == true
                    if (isWinningCell) {
                        setBackgroundResource(R.drawable.cell_background_win)
                    } else {
                        setBackgroundResource(R.drawable.cell_background)
                    }
                }
            }
        }

        // Theme icon position
        themeIcon.text = "☀️"
        themeIcon.translationX = 0f
    }

    private fun applyDarkTheme() {
        // Background
        mainLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_background))

        // Header stays yellow
        headerLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.primary))

        // Mode toggle
        modeToggleContainer.setBackgroundResource(R.drawable.toggle_background_dark)
        symbolToggle.setBackgroundResource(R.drawable.toggle_background_dark)

        // Scoreboard
        scoreCardX.setBackgroundResource(R.drawable.score_card_x_dark)
        scoreCardO.setBackgroundResource(R.drawable.score_card_o_dark)
        textVs.setTextColor(ContextCompat.getColor(this, R.color.dark_text_secondary))

        // Status text
        textStatus.setTextColor(ContextCompat.getColor(this, R.color.dark_text))

        // Cells - check if it's a winning cell or regular cell
        val winningCells = gameLogic.getWinningCells()
        for (i in 0..2) {
            for (j in 0..2) {
                cells[i][j]?.apply {
                    setTextColor(ContextCompat.getColor(this@MainActivity, R.color.dark_text))

                    // Check if this is a winning cell
                    val isWinningCell = winningCells?.any { it.first == i && it.second == j } == true
                    if (isWinningCell) {
                        setBackgroundResource(R.drawable.cell_background_win)
                    } else {
                        setBackgroundResource(R.drawable.cell_background_dark)
                    }
                }
            }
        }

        // Theme icon position
        themeIcon.text = "🌙"
        themeIcon.translationX = (themeToggle.width - themeIcon.width - 8).toFloat()
    }

    private fun setModeToggle(isPvP: Boolean) {
        if (isPvP) {
            btnPvP.setBackgroundResource(R.drawable.toggle_item_active)
            btnPvP.setTextColor(ContextCompat.getColor(this, R.color.black))
            btnPvC.setBackgroundResource(R.drawable.toggle_item_inactive)
            btnPvC.setTextColor(ContextCompat.getColor(this, R.color.toggle_inactive_text))
        } else {
            btnPvC.setBackgroundResource(R.drawable.toggle_item_active)
            btnPvC.setTextColor(ContextCompat.getColor(this, R.color.black))
            btnPvP.setBackgroundResource(R.drawable.toggle_item_inactive)
            btnPvP.setTextColor(ContextCompat.getColor(this, R.color.toggle_inactive_text))
        }
    }

    private fun setSymbolToggle(isX: Boolean) {
        if (isX) {
            btnChooseX.setBackgroundResource(R.drawable.toggle_item_active)
            btnChooseX.setTextColor(ContextCompat.getColor(this, R.color.black))
            btnChooseO.setBackgroundResource(R.drawable.toggle_item_inactive)
            btnChooseO.setTextColor(ContextCompat.getColor(this, R.color.toggle_inactive_text))
        } else {
            btnChooseO.setBackgroundResource(R.drawable.toggle_item_active)
            btnChooseO.setTextColor(ContextCompat.getColor(this, R.color.black))
            btnChooseX.setBackgroundResource(R.drawable.toggle_item_inactive)
            btnChooseX.setTextColor(ContextCompat.getColor(this, R.color.toggle_inactive_text))
        }
    }

    private fun onCellClicked(row: Int, col: Int) {
        if (!gameLogic.isGameActive()) {
            return
        }
        handleMove(row, col, isComputerMove = false)
    }

    private fun handleMove(row: Int, col: Int, isComputerMove: Boolean) {
        val currentPlayer = if (gameLogic.isXTurn()) "X" else "O"

        if (vsComputer && !isComputerMove && currentPlayer != playerSymbol) {
            return
        }

        if (gameLogic.makeMove(row, col)) {
            // Update cell with animation
            cells[row][col]?.apply {
                text = currentPlayer
                setTextColor(ContextCompat.getColor(
                    this@MainActivity,
                    if (isDarkTheme) R.color.dark_text else R.color.light_text
                ))
                val popAnimation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.cell_pop)
                startAnimation(popAnimation)
            }

            // Check result
            val result = gameLogic.checkWinner()

            when (result) {
                "X" -> {
                    gameLogic.addPointX()
                    updateScores()
                    highlightWinningCells()
                    textStatus.text = "X wins!"
                }
                "O" -> {
                    gameLogic.addPointO()
                    updateScores()
                    highlightWinningCells()
                    textStatus.text = "O wins!"
                }
                "Draw" -> {
                    textStatus.text = "It's a draw!"
                }
                else -> {
                    gameLogic.switchTurn()
                    updateTurnDisplay()

                    if (vsComputer && gameLogic.isGameActive()) {
                        val nextPlayer = if (gameLogic.isXTurn()) "X" else "O"
                        if (nextPlayer == computerSymbol) {
                            handler.postDelayed({
                                computerMove()
                            }, 500)
                        }
                    }
                }
            }
        }
    }

    private fun computerMove() {
        if (!gameLogic.isGameActive()) return

        val move = computerAI?.findBestMove(gameLogic)
        if (move != null) {
            handleMove(move.first, move.second, isComputerMove = true)
        }
    }

    private fun updateTurnDisplay() {
        val currentPlayer = if (gameLogic.isXTurn()) "X" else "O"
        textStatus.text = "$currentPlayer's turn"
    }

    private fun updateScores() {
        textScoreX.text = "${gameLogic.getScoreX()}"
        textScoreO.text = "${gameLogic.getScoreO()}"
    }

    private fun resetScores() {
        gameLogic = GameLogic()
        updateScores()
    }

    private fun highlightWinningCells() {
        val winningCells = gameLogic.getWinningCells()
        if (winningCells != null) {
            for (cell in winningCells) {
                cells[cell.first][cell.second]?.setBackgroundResource(R.drawable.cell_background_win)
            }
        }
    }

    private fun resetBoard() {
        gameLogic.initializeBoard()
        for (i in 0..2) {
            for (j in 0..2) {
                cells[i][j]?.apply {
                    text = ""
                    setBackgroundResource(
                        if (isDarkTheme) R.drawable.cell_background_dark
                        else R.drawable.cell_background
                    )
                }
            }
        }
        updateTurnDisplay()

        if (vsComputer && playerSymbol == "O") {
            handler.postDelayed({
                computerMove()
            }, 500)
        }
    }

    private fun restartGame() {
        resetBoard()
        updateTurnDisplay()
    }
}