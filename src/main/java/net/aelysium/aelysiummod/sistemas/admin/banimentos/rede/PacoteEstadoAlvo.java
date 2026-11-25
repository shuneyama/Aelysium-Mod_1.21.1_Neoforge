package net.aelysium.aelysiummod.sistemas.admin.banimentos.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record PacoteEstadoAlvo(String alvoTexto, int tipo, String nome, int total, List<String> todos, List<Item> itens) implements CustomPacketPayload {

    public static final int NENHUM = 0;
    public static final int ONLINE = 1;
    public static final int OFFLINE = 2;
    public static final int VARIOS = 3;

    public record Item(ResourceLocation item, int quantidade, String por, String em, List<String> nomes) {
    }

    public static final Type<PacoteEstadoAlvo> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "banimentos_estado_alvo"));

    public static final StreamCodec<FriendlyByteBuf, PacoteEstadoAlvo> STREAM_CODEC = StreamCodec.of(
            (buf, pkt) -> {
                buf.writeUtf(pkt.alvoTexto(), 128);
                buf.writeVarInt(pkt.tipo());
                buf.writeUtf(pkt.nome(), 64);
                buf.writeVarInt(pkt.total());
                buf.writeVarInt(pkt.todos().size());
                for (String n : pkt.todos()) buf.writeUtf(n, 64);
                buf.writeVarInt(pkt.itens().size());
                for (Item i : pkt.itens()) {
                    buf.writeResourceLocation(i.item());
                    buf.writeVarInt(i.quantidade());
                    buf.writeUtf(i.por(), 64);
                    buf.writeUtf(i.em(), 64);
                    buf.writeVarInt(i.nomes().size());
                    for (String n : i.nomes()) buf.writeUtf(n, 64);
                }
            },
            buf -> {
                String alvoTexto = buf.readUtf(128);
                int tipo = buf.readVarInt();
                String nome = buf.readUtf(64);
                int total = buf.readVarInt();
                int nt = buf.readVarInt();
                List<String> todos = new ArrayList<>(nt);
                for (int i = 0; i < nt; i++) todos.add(buf.readUtf(64));
                int n = buf.readVarInt();
                List<Item> itens = new ArrayList<>(n);
                for (int i = 0; i < n; i++) {
                    ResourceLocation id = buf.readResourceLocation();
                    int qtd = buf.readVarInt();
                    String por = buf.readUtf(64);
                    String em = buf.readUtf(64);
                    int nn = buf.readVarInt();
                    List<String> nomes = new ArrayList<>(nn);
                    for (int k = 0; k < nn; k++) nomes.add(buf.readUtf(64));
                    itens.add(new Item(id, qtd, por, em, nomes));
                }
                return new PacoteEstadoAlvo(alvoTexto, tipo, nome, total, todos, itens);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
