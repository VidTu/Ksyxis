package ru.vidtu.ksyxis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;

public class Ksyxis implements ModInitializer {
	public static final Logger LOG = LogManager.getLogger("Ksyxis");
	@Override
	public void onInitialize() {
		LOG.warn("Ksyxis will speedup your world loading. :P");
		LOG.warn("BUT IT MAY CAUSE ISSUES (INCLUDING COMPAT). DON'T ASK OTHER MODS' AUTHORS FOR COMPAT!");
	}
}
