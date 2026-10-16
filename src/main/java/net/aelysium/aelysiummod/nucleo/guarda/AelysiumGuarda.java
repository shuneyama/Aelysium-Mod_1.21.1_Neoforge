package net.aelysium.aelysiummod.nucleo.guarda;

import net.minecraft.server.MinecraftServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class AelysiumGuarda {

    private static final String HOST_ESPERADO = "sp-17.magnohost.com.br";
    private static final String IP_FALLBACK = "198.89.99.83";
    private static final int PORTA_ESPERADA = 25542;

    public static final boolean MODO_TESTE = !net.neoforged.fml.loading.FMLEnvironment.production
            && !"on".equalsIgnoreCase(System.getProperty("aelysium.guarda"));

    private static boolean autorizado = MODO_TESTE;

    public static void verificar(MinecraftServer server) {
        if (MODO_TESTE) {
            autorizado = true;
            return;
        }
        autorizado = false;

        if (server.getPort() != PORTA_ESPERADA) return;

        try {
            Set<String> ipsEsperados = new HashSet<>();
            ipsEsperados.add(IP_FALLBACK);
            try {
                InetAddress[] enderecos = InetAddress.getAllByName(HOST_ESPERADO);
                for (InetAddress addr : enderecos) {
                    ipsEsperados.add(addr.getHostAddress());
                }
            } catch (Exception ignored) {}

            String ipPublico = obterIpPublico();
            if (ipPublico != null && ipsEsperados.contains(ipPublico)) {
                autorizado = true;
                return;
            }

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    NetworkInterface ni = interfaces.nextElement();
                    Enumeration<InetAddress> endLocal = ni.getInetAddresses();
                    while (endLocal.hasMoreElements()) {
                        String ip = endLocal.nextElement().getHostAddress();
                        if (ipsEsperados.contains(ip)) {
                            autorizado = true;
                            return;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

    }

    private static String obterIpPublico() {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.ipify.org").openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                return reader.readLine().trim();
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isAutorizado() {
        return autorizado;
    }

    public static boolean isAutorizadoAgora() {
        if (net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT) {
            return EstadoGuardaCliente.isAutorizado();
        }
        return autorizado;
    }
}
