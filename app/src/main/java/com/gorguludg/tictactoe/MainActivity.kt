package com.gorguludg.tictactoe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val cells = Array(3) { Array<TextView?>(3) { null } }
    private lateinit var btnRestart: TextView
    private lateinit var textStatus: TextView
    private lateinit var textScoreX: TextView
    private lateinit var textScoreO: TextView

    // Mode toggle
    private lateinit var btnPvP: TextView
    private lateinit var btnPvC: TextView

    // Symbol toggle
    private lateinit var symbolToggle: LinearLayout
    private lateinit var btnChooseX: TextView
    private lateinit var btnChooseO: TextView

    private lateinit var gameLogic: GameLogic
    private var computerAI: ComputerAI? = null

    // Game settings
    private var vsComputer = false
    private var playerSymbol = "X"
    private var computerSymbol = "O"

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameLogic = GameLogic()
        initializeUI()
        updateTurnDisplay()
    }

    private fun initializeUI() {
        // Text views
        textStatus = findViewById(R.id.textStatus)
        textScoreX = findViewById(R.id.textScoreX)
        textScoreO = findViewById(R.id.textScoreO)
        btnRestart = findViewById(R.id.btnRestart)

        // Mode toggle
        btnPvP = findViewById(R.id.btnPvP)
        btnPvC = findViewById(R.id.btnPvC)

        // Symbol toggle
        symbolToggle = findViewById(R.id.symbolToggle)
        btnChooseX = findViewById(R.id.btnChooseX)
        btnChooseO = findViewById(R.id.btnChooseO)

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

        // Restart button
        btnRestart.setOnClickListener {
            restartGame()
        }
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
            // Update cell with animation and color
            cells[row][col]?.apply {
                text = currentPlayer
                setTextColor(ContextCompat.getColor(
                    this@MainActivity,
                    if (currentPlayer == "X") R.color.player_x else R.color.player_o
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
                    textStatus.setTextColor(ContextCompat.getColor(this, R.color.black))
                }
                "O" -> {
                    gameLogic.addPointO()
                    updateScores()
                    highlightWinningCells()
                    textStatus.text = "O wins!"
                    textStatus.setTextColor(ContextCompat.getColor(this, R.color.black))
                }
                "Draw" -> {
                    textStatus.text = "It's a draw!"
                    textStatus.setTextColor(ContextCompat.getColor(this, R.color.black))
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
        textStatus.setTextColor(ContextCompat.getColor(this, R.color.black))
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
                    setBackgroundResource(R.drawable.cell_background)
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