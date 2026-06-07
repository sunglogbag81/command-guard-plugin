package kr.openclaw.commandguard;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class CommandGuardPlugin extends JavaPlugin implements Listener {
    private final Set<String> blockedCommands = new HashSet<>();
    private String blockedMessage;
    private boolean hideFromTabComplete;
    private boolean blockConsoleCommands;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("CommandGuard enabled. Blocked commands: " + blockedCommands.size());
    }

    private void loadSettings() {
        reloadConfig();
        blockedCommands.clear();
        for (String command : getConfig().getStringList("blocked-commands")) {
            String normalized = normalizeCommandName(command);
            if (!normalized.isEmpty()) {
                blockedCommands.add(normalized);
                String base = stripNamespace(normalized);
                if (!base.isEmpty()) {
                    blockedCommands.add(base);
                }
            }
        }
        blockedMessage = color(getConfig().getString("blocked-message", "&c이 명령어는 사용할 수 없습니다."));
        hideFromTabComplete = getConfig().getBoolean("hide-from-tab-complete", true);
        blockConsoleCommands = getConfig().getBoolean("block-console-commands", false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("commandguard.bypass")) return;

        String commandName = extractCommandName(event.getMessage());
        if (isBlocked(commandName)) {
            event.setCancelled(true);
            player.sendMessage(blockedMessage);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event) {
        if (!blockConsoleCommands) return;

        CommandSender sender = event.getSender();
        if (!(sender instanceof ConsoleCommandSender) && sender.hasPermission("commandguard.bypass")) return;

        String commandName = extractCommandName(event.getCommand());
        if (isBlocked(commandName)) {
            event.setCancelled(true);
            sender.sendMessage(blockedMessage);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTabComplete(TabCompleteEvent event) {
        if (!hideFromTabComplete) return;
        if (!(event.getSender() instanceof Player player)) return;
        if (player.hasPermission("commandguard.bypass")) return;

        String buffer = event.getBuffer();
        if (buffer == null || !buffer.startsWith("/")) return;

        String typedCommand = extractCommandName(buffer);
        if (isBlocked(typedCommand)) {
            event.setCancelled(true);
            event.setCompletions(List.of());
            return;
        }

        List<String> filtered = new ArrayList<>();
        for (String completion : event.getCompletions()) {
            String completionCommand = extractCommandName(completion.startsWith("/") ? completion : "/" + completion);
            if (!isBlocked(completionCommand)) {
                filtered.add(completion);
            }
        }
        event.setCompletions(filtered);
    }

    private boolean isBlocked(String commandName) {
        String normalized = normalizeCommandName(commandName);
        if (normalized.isEmpty()) return false;
        return blockedCommands.contains(normalized) || blockedCommands.contains(stripNamespace(normalized));
    }

    private String extractCommandName(String raw) {
        if (raw == null) return "";
        String text = raw.trim();
        if (text.startsWith("/")) text = text.substring(1);
        int space = text.indexOf(' ');
        if (space >= 0) text = text.substring(0, space);
        return normalizeCommandName(text);
    }

    private String normalizeCommandName(String command) {
        if (command == null) return "";
        String text = command.trim().toLowerCase(Locale.ROOT);
        while (text.startsWith("/")) text = text.substring(1);
        int space = text.indexOf(' ');
        if (space >= 0) text = text.substring(0, space);
        return text;
    }

    private String stripNamespace(String command) {
        String normalized = normalizeCommandName(command);
        int colon = normalized.indexOf(':');
        if (colon >= 0 && colon + 1 < normalized.length()) {
            return normalized.substring(colon + 1);
        }
        return normalized;
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message == null ? "" : message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("commandguard.reload")) {
                sender.sendMessage(color("&c권한이 없습니다."));
                return true;
            }
            loadSettings();
            sender.sendMessage(color("&aCommandGuard 설정을 다시 불러왔습니다."));
            return true;
        }
        sender.sendMessage(color("&e사용법: /" + label + " reload"));
        return true;
    }
}
