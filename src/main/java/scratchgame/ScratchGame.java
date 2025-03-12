package scratchgame;

import com.fasterxml.jackson.databind.ObjectMapper;
import scratchgame.model.Config;

import java.io.File;
import java.util.Scanner;

public class ScratchGame {

    public static void main(String[] args) throws Exception {
        // Load config.json path from arguments or use default
        String configPath = null;
        for (int i = 0; i < args.length; i++) {
            if ("--config".equals(args[i]) && i + 1 < args.length) {
                configPath = args[i + 1];
            }
        }
        if (configPath == null) {
            configPath = "config.json";
        }

        // Read configuration file
        ObjectMapper mapper = new ObjectMapper();
        Config config = mapper.readValue(new File(configPath), Config.class);

        Scanner scanner = new Scanner(System.in);

        // Game loop
        while (true) {
            GameSession.playRound(scanner, config);

            System.out.print("\nDo you want to play again? (y/n): ");
            String answer = scanner.nextLine().trim().toLowerCase();
            if (!answer.equals("y") && !answer.equals("yes")) {
                System.out.println("Thanks for playing! Goodbye.");
                break;
            }
        }
    }
}