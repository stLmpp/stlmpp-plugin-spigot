package com.stlmpp.spigot.plugins.events;

import com.stlmpp.spigot.plugins.StlmppPlugin;
import com.stlmpp.spigot.plugins.utils.Tick;
import com.stlmpp.spigot.plugins.utils.Util;
import java.util.Stack;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

public class NetherPortalLeakingTask extends BukkitRunnable {

  private final Stack<Block> replacementBlocks;

  public NetherPortalLeakingTask(StlmppPlugin plugin, Stack<Block> replacementBlocks) {
    this.replacementBlocks = replacementBlocks;
    this.runTaskTimer(plugin, 0, Tick.fromSeconds(3));
  }

  @Override
  public void run() {
    final var block = this.replacementBlocks.pop();
    block.setType(Util.convertToNetherMaterial(block.getType()));
    if (this.replacementBlocks.size() == 0) {
      this.cancel();
    }
  }
}
