package me.robomwm.MountainDewritoes.adminai;

record AiProvider(String name, String protocol, String endpoint, String model, String apiKey, int timeoutSeconds) {}
