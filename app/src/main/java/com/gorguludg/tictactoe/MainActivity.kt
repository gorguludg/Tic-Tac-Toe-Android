package com.yourname.tictactoe

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val buttons = Array(3) { Array<Button?>(3) { null } }
    private lateinit var btnRestart: Button
    private lateinit var textStatus: TextView
    private lateinit var textScoreX: TextView
    private lateinit var textScoreO: TextView
    private lateinit var textGitHub: TextView

    // Mode toggle buttons
    private lateinit var btnPvP: Button
    private lateinit var btnPvC: Button

    // Symbol toggle buttons and container
    private lateinit var symbolToggle: LinearLayout
    private lateinit var btnChooseX: Button
    private lateinit var btnChooseO: Button

    private lateinit var gameLogic: GameLogic
    private var computerAI: ComputerAI? = null

    // Game mode settings
    private var vsComputer = false
    private var playerSymbol = "X"
    private var computerSymbol = "O"

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize game logic
        gameLogic = GameLogic()

        // Initialize UI components
        initializeUI()

        // Update initial display
        updateTurnDisplay()
    }

    private fun initializeUI() {
        // Find text views
        textStatus = findViewById(R.id.textStatus)
        textScoreX = findViewById(R.id.textScoreX)
        textScoreO = findViewById(R.id.textScoreO)
        textGitHub = findViewById(R.id.textGitHub)
        btnRestart = findViewById(R.id.btnRestart)

        // Mode toggle buttons
        btnPvP = findViewById(R.id.btnPvP)
        btnPvC = findViewById(R.id.btnPvC)

        // Symbol toggle
        symbolToggle = findViewById(R.id.symbolToggle)
        btnChooseX = findViewById(R.id.btnChooseX)
        btnChooseO = findViewById(R.id.btnChooseO)

        // Find and set up game board buttons
        buttons[0][0] = findViewById(R.id.btn00)
        buttons[0][1] = findViewById(R.id.btn01)
        buttons[0][2] = findViewById(R.id.btn02)
        buttons[1][0] = findViewById(R.id.btn10)
        buttons[1][1] = findViewById(R.id.btn11)
        buttons[1][2] = findViewById(R.id.btn12)
        buttons[2][0] = findViewById(R.id.btn20)
        buttons[2][1] = findViewById(R.id.btn21)
        buttons[2][2] = findViewById(R.id.btn22)

        // Set click listeners for all game buttons
        for (i in 0..2) {
            for (j in 0..2) {
                buttons[i][j]?.setOnClickListener(this)
            }
        }

        // Mode toggle listeners
        btnPvP.setOnClickListener {
            setActiveToggle(btnPvP, btnPvC)
            vsComputer = false
            symbolToggle.visibility = View.GONE
            resetScores()
            restartGame()
        }

        btnPvC.setOnClickListener {
            setActiveToggle(btnPvC, btnPvP)
            vsComputer = true
            symbolToggle.visibility = View.VISIBLE
            computerAI = ComputerAI(computerSymbol, playerSymbol)
            resetScores()
            restartGame()
        }

        // Symbol toggle listeners
        btnChooseX.setOnClickListener {
            setActiveToggle(btnChooseX, btnChooseO)
            playerSymbol = "X"
            computerSymbol = "O"
            computerAI = ComputerAI(computerSymbol, playerSymbol)
            resetScores()
            restartGame()
        }

        btnChooseO.setOnClickListener {
            setActiveToggle(btnChooseO, btnChooseX)
            playerSymbol = "O"
            computerSymbol = "X"
            computerAI = ComputerAI(computerSymbol, playerSymbol)
            resetScores()
            restartGame()
        }

        // Restart button listener
        btnRestart.setOnClickListener {
            restartGame()
        }

        // GitHub link listener
        textGitHub.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/gorguludg/Tic-Tac-Toe-Android"))
            startActivity(intent)
        }

        // Set initial toggle states
        btnPvP.isSelected = true
        btnChooseX.isSelected = true
    }

    private fun setActiveToggle(activeBtn: Button, inactiveBtn: Button) {
        activeBtn.isSelected = true
        activeBtn.setTextColor(ContextCompat.getColor(this, android.R.color.white))

        inactiveBtn.isSelected = false
        inactiveBtn.setTextColor(ContextCompat.getColor(this, R.color.blue_primary))
    }

    override fun onClick(v: View?) {
        // Find which button was clicked
        var row = -1
        var col = -1

        for (i in 0..2) {
            for (j in 0..2) {
                if (v?.id == buttons[i][j]?.id) {
                    row = i
                    col = j
                }
            }
        }

        if (row == -1 || col == -1) return

        // Make the move
        handleMove(row, col)
    }

    private fun handleMove(row: Int, col: Int) {
        val currentPlayer = if (gameLogic.isXTurn()) "X" else "O"

        // Check if it's player's turn in PvC mode
        if (vsComputer && currentPlayer != playerSymbol) {
            return // Computer's turn, ignore human clicks
        }

        if (gameLogic.makeMove(row, col)) {
            // Update button text with animation
            buttons[row][col]?.apply {
                text = currentPlayer
                val popAnimation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.cell_pop)
                startAnimation(popAnimation)
            }

            // Check for winner
            val result = gameLogic.checkWinner()

            when (result) {
                "X" -> {
                    gameLogic.addPointX()
                    updateScores()
                    highlightWinningCells()
                    textStatus.text = "X wins!"
                    showResultDialog("X wins! 🎉")
                }
                "O" -> {
                    gameLogic.addPointO()
                    updateScores()
                    highlightWinningCells()
                    textStatus.text = "O wins!"
                    showResultDialog("O wins! 🎉")
                }
                "Draw" -> {
                    textStatus.text = "It's a draw!"
                    showResultDialog("It's a draw! 🤝")
                }
                else -> {
                    // Game continues - switch turn
                    gameLogic.switchTurn()
                    updateTurnDisplay()

                    // If playing against computer, let it make a move
                    if (vsComputer && gameLogic.isGameActive()) {
                        val nextPlayer = if (gameLogic.isXTurn()) "X" else "O"
                        if (nextPlayer == computerSymbol) {
                            handler.postDelayed({
                                computerMove()
                            }, 500) // 500ms delay for computer move
                        }
                    }
                }
            }
        }
    }

    private fun computerMove() {
        val move = computerAI?.findBestMove(gameLogic)
        if (move != null) {
            handleMove(move.first, move.second)
        }
    }

    private fun updateTurnDisplay() {
        val currentPlayer = if (gameLogic.isXTurn()) "X" else "O"
        textStatus.text = "Player $currentPlayer's turn"
    }

    private fun updateScores() {
        textScoreX.text = "X Wins: ${gameLogic.getScoreX()}"
        textScoreO.text = "O Wins: ${gameLogic.getScoreO()}"
    }

    private fun resetScores() {
        gameLogic = GameLogic() // This resets scores
        updateScores()
    }

    private fun highlightWinningCells() {
        val winningCells = gameLogic.getWinningCells()
        if (winningCells != null) {
            for (cell in winningCells) {
                buttons[cell.first][cell.second]?.apply {
                    setBackgroundResource(R.drawable.cell_background_win)
                }
            }
        }
    }

    private fun showResultDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage(message)
            .setPositiveButton("Play Again") { _, _ ->
                resetBoard()
            }
            .setNegativeButton("Close", null)
            .setCancelable(false)
            .show()
    }

    private fun resetBoard() {
        gameLogic.initializeBoard()
        for (i in 0..2) {
            for (j in 0..2) {
                buttons[i][j]?.apply {
                    text = ""
                    setBackgroundResource(R.drawable.cell_background)
                }
            }
        }
        updateTurnDisplay()

        // If computer should start (player chose O in PvC mode)
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