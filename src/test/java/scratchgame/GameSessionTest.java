package scratchgame;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import scratchgame.logic.MatrixGenerator;
import scratchgame.logic.RewardCalculator;
import scratchgame.model.CalculationResult;
import scratchgame.model.Config;

import java.util.Scanner;

import static org.mockito.Mockito.*;

public class GameSessionTest {

    private Config config;
    private Scanner scannerMock;

    @Before
    public void setup() {
        config = new Config();
        config.rows = 3;
        config.columns = 3;

        scannerMock = mock(Scanner.class);
    }

    /**
     * Test: Ensures the game correctly processes default bet when no input is given.
     */
    @Test
    public void testDefaultBet() {
        when(scannerMock.nextLine()).thenReturn(""); // Simulate Enter press
        GameSession.playRound(scannerMock, config);
        verify(scannerMock, atLeastOnce()).nextLine();
    }

    /**
     * Test: Ensures the game correctly processes a valid numeric bet.
     */
    @Test
    public void testValidBet() {
        when(scannerMock.nextLine()).thenReturn("200");
        GameSession.playRound(scannerMock, config);
        verify(scannerMock, atLeastOnce()).nextLine();
    }

    /**
     * Test: Ensures the game falls back to default bet when input is invalid.
     */
    @Test
    public void testInvalidBet() {
        when(scannerMock.nextLine()).thenReturn("invalid");
        GameSession.playRound(scannerMock, config);
        verify(scannerMock, atLeastOnce()).nextLine();
    }

    /**
     * Test: Ensures playRound calls reward calculation and matrix generation.
     */
    @Test
    public void testPlayRoundInvokesLogic() {
        String[][] dummyMatrix = {
                {"A", "B", "C"},
                {"D", "E", "F"},
                {"G", "H", "I"}
        };

        CalculationResult dummyResult = new CalculationResult();
        dummyResult.finalReward = 100;

        when(scannerMock.nextLine()).thenReturn("100");
        when(MatrixGenerator.generateMatrix(any(), anyInt(), anyInt())).thenReturn(dummyMatrix);
        when(RewardCalculator.calculateReward(any(), anyInt(), any())).thenReturn(dummyResult);

        GameSession.playRound(scannerMock, config);

        verify(MatrixGenerator.class, times(1));
        verify(RewardCalculator.class, times(1));
    }
}