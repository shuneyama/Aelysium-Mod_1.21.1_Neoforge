package net.aelysium.aelysiummod.registro;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.conteudo.bloco.decoracao.BlocoColuna;
import net.aelysium.aelysiummod.conteudo.bloco.decoracao.BlocoConcha;
import net.aelysium.aelysiummod.conteudo.bloco.decoracao.BlocoDecorativoBase;
import net.aelysium.aelysiummod.conteudo.bloco.decoracao.BlocoVidroEixo;
import net.aelysium.aelysiummod.conteudo.bloco.decoracao.DecorHorizontal;
import net.aelysium.aelysiummod.conteudo.bloco.decoracao.DecorHorizontalLuz;
import net.aelysium.aelysiummod.conteudo.bloco.decoracao.DecorSeis;
import net.aelysium.aelysiummod.conteudo.bloco.decoracao.DecorSeisLuz;
import net.aelysium.aelysiummod.conteudo.bloco.decoracao.DecorSimples;
import net.aelysium.aelysiummod.conteudo.bloco.decoracao.DecorSimplesLuz;
import net.aelysium.aelysiummod.conteudo.bloco.decoracao.MaterialDecoracao;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static net.aelysium.aelysiummod.conteudo.bloco.decoracao.MaterialDecoracao.*;

public final class ModBlocosDecoracao {
    public static final DeferredRegister.Blocks BLOCOS = DeferredRegister.createBlocks(AelysiumMod.MOD_ID);
    public static final DeferredRegister.Items ITENS = DeferredRegister.createItems(AelysiumMod.MOD_ID);
    public static final Map<String, Entrada> ENTRADAS = new LinkedHashMap<>();

    public enum Categoria {
        COMUM, LAJE, ESCADA, MURO, PORTA, ALCAPAO
    }

    public enum TipoDecor {
        SIMPLES, SIMPLES_LUZ, HORIZONTAL, HORIZONTAL_LUZ, SEIS, SEIS_LUZ
    }

    public record Entrada(String id, MaterialDecoracao material, Categoria categoria, DeferredBlock<Block> bloco) {
    }

    static {
        cubo("bloco_de_perola", PEROLA, 0);
        cubo("bloco_de_perola_negra", PEROLA, 0);
        cubo("tijolos_de_perola", PEROLA, 0);
        cubo("tijolos_de_perola_negra", PEROLA, 0);
        cubo("ladrilhos_de_perola_sangrenta", PEROLA, 0);
        cubo("ladrilhos_de_perola_jade", PEROLA, 0);
        cubo("ladrilhos_de_perola_mistos", PEROLA, 0);
        cubo("ladrilhos_de_perola_estrelados", PEROLA, 0);
        cubo("ladrilhos_de_perola", PEROLA, 0);
        cubo("ladrilhos_de_perola_rachados", PEROLA, 0);
        cubo("ladrilhos_de_perola_rachados_2", PEROLA, 0);
        cubo("ladrilhos_de_perola_rachados_3", PEROLA, 0);
        cubo("ladrilhos_de_perola_rachados_4", PEROLA, 0);
        cubo("ladrilhos_de_perola_rachados_5", PEROLA, 0);
        cubo("ladrilhos_de_perola_rachados_6", PEROLA, 0);
        cubo("bloco_de_perola_cinzelado", PEROLA, 0);
        cubo("bloco_de_perola_negra_cinzelado", PEROLA, 0);
        cubo("bloco_de_perola_cortado", PEROLA, 0);
        cubo("bloco_de_perola_negra_cortado", PEROLA, 0);
        cubo("perola_negra_dourada", PEROLA, 0);
        cubo("bloco_de_perola_liso", PEROLA, 0);
        cubo("bloco_de_perola_negra_liso", PEROLA, 0);
        cubo("piso_de_marmore", MARMORE, 0);
        cubo("marmore_polido", MARMORE, 0);
        cubo("marmore_bruto", MARMORE, 0);
        laje("laje_de_perola", PEROLA);
        escada("escada_de_perola", "bloco_de_perola", PEROLA);
        muro("muro_de_perola", "bloco_de_perola", PEROLA);
        laje("laje_de_tijolos_de_perola", PEROLA);
        escada("escada_de_tijolos_de_perola", "tijolos_de_perola", PEROLA);
        muro("muro_de_tijolos_de_perola", "tijolos_de_perola", PEROLA);
        laje("laje_de_perola_negra", PEROLA);
        escada("escada_de_perola_negra", "bloco_de_perola_negra", PEROLA);
        muro("muro_de_perola_negra", "bloco_de_perola_negra", PEROLA);
        laje("laje_de_tijolos_de_perola_negra", PEROLA);
        escada("escada_de_tijolos_de_perola_negra", "tijolos_de_perola_negra", PEROLA);
        muro("muro_de_tijolos_de_perola_negra", "tijolos_de_perola_negra", PEROLA);
        laje("laje_de_perola_lisa", PEROLA);
        escada("escada_de_perola_lisa", "bloco_de_perola_liso", PEROLA);
        muro("muro_de_perola_lisa", "bloco_de_perola_liso", PEROLA);
        laje("laje_de_perola_negra_lisa", PEROLA);
        escada("escada_de_perola_negra_lisa", "bloco_de_perola_negra_liso", PEROLA);
        muro("muro_de_perola_negra_lisa", "bloco_de_perola_negra_liso", PEROLA);
        laje("laje_de_marmore_polido", MARMORE);
        escada("escada_de_marmore_polido", "marmore_polido", MARMORE);
        muro("muro_de_marmore_polido", "marmore_polido", MARMORE);
        pilar("pilar_de_perola", PEROLA);
        pilar("pilar_de_perola_negra", PEROLA);
        pilar("piso_de_pilar_de_marmore", MARMORE);
        coluna("pilar_de_marmore", MARMORE);
        vidro("glowstone_cristalizado_emoldurado", CRISTAL, 15);
        vidro("vidro_fresnel", VIDRO, 0);
        vidro("vidro_vermelhao", CRISTAL, 0);
        vidro("vidro_vermelhao_emoldurado", CRISTAL, 0);
        vidro("bloco_de_cristal_de_prismarinho", CRISTAL, 15);
        painel("painel_de_glowstone_cristalizado_emoldurado", CRISTAL, 15);
        painel("painel_de_vidro_fresnel", VIDRO, 0);
        painel("painel_de_vidro_vermelhao", CRISTAL, 0);
        painel("painel_de_vidro_vermelhao_emoldurado", CRISTAL, 0);
        painel("painel_de_cristal_de_prismarinho", CRISTAL, 15);
        vidroEixo("vidro_perolado", VIDRO);
        vidroEixo("vidro_perolado_azul_celeste", VIDRO);
        vidroEixo("vidro_perolado_madeira_negra", VIDRO);
        vidroEixo("vidro_perolado_ferro", VIDRO);
        vidroEixo("vidro_perolado_ensolarado", VIDRO);
        vidroEixo("vidro_perolado_verdejante", VIDRO);
        vidroEixo("vidro_perolado_madeira_branca", VIDRO);
        painel("painel_de_vidro_perolado", VIDRO, 0);
        painel("painel_de_vidro_perolado_azul_celeste", VIDRO, 0);
        painel("painel_de_vidro_perolado_madeira_negra", VIDRO, 0);
        painel("painel_de_vidro_perolado_ferro", VIDRO, 0);
        painel("painel_de_vidro_perolado_ensolarado", VIDRO, 0);
        painel("painel_de_vidro_perolado_verdejante", VIDRO, 0);
        painel("painel_de_vidro_perolado_madeira_branca", VIDRO, 0);
        folhas("folhas_de_glicinia_ancia", FOLHAGEM);
        decor("lampada_de_flor", LAMPADA, TipoDecor.HORIZONTAL_LUZ, 15, 1);
        decor("lampada_de_parede", LAMPADA, TipoDecor.HORIZONTAL_LUZ, 15, 1);
        decor("lampada", LAMPADA, TipoDecor.SEIS_LUZ, 15, 1);
        decor("lampiao_de_ferro_de_parede", LAMPADA, TipoDecor.HORIZONTAL_LUZ, 15, 1);
        decor("lampiao_de_ferro_de_teto", LAMPADA, TipoDecor.SIMPLES_LUZ, 15, 4);
        decor("lampada_fresnel", LAMPADA, TipoDecor.SIMPLES, 15, 0);
        decor("castical", LAMPADA, TipoDecor.SIMPLES, 15, 3);
        decor("haste_eterea", HASTE, TipoDecor.SEIS, 8, 0);
        decor("haste_lunar", HASTE, TipoDecor.SEIS, 11, 0);
        decor("haste_solar", HASTE, TipoDecor.SEIS, 13, 0);
        decor("haste_estelar", HASTE, TipoDecor.SEIS, 13, 0);
        decor("haste_terrestre", HASTE, TipoDecor.SEIS, 8, 0);
        decor("estrela_do_mar", CORAL, TipoDecor.SEIS, 0, 1);
        decor("estrela_do_mar_laranja_2", CORAL, TipoDecor.SEIS, 0, 1);
        decor("estrela_do_mar_rosa_1", CORAL, TipoDecor.SEIS, 0, 1);
        decor("estrela_do_mar_rosa_2", CORAL, TipoDecor.SEIS, 0, 1);
        decor("estrela_do_mar_roxa_1", CORAL, TipoDecor.SEIS, 0, 1);
        decor("estrela_do_mar_roxa_2", CORAL, TipoDecor.SEIS, 0, 1);
        decor("estrela_do_mar_seca", CORAL, TipoDecor.SEIS, 0, 1);
        decor("estrela_do_mar_seca_2", CORAL, TipoDecor.SEIS, 0, 1);
        decor("boia_salva_vidas", TECIDO, TipoDecor.SEIS, 0, 1);
        decor("ameijoa_filhote", CONCHA, TipoDecor.HORIZONTAL, 0, 0);
        decor("perola_branca", CONCHA, TipoDecor.SEIS, 0, 1);
        decor("perola_negra", CONCHA, TipoDecor.SEIS, 0, 1);
        decor("petalas_de_glicinia", PETALAS, TipoDecor.SIMPLES, 0, 0);
        decor("petalas_de_glicinia_ancia", PETALAS, TipoDecor.SIMPLES, 0, 0);
        concha("ameijoa", CONCHA, "perola_branca");
        concha("ameijoa_gigante", CONCHA, "bloco_de_perola");
    }

    private ModBlocosDecoracao() {
    }

    public static void registrar(IEventBus barramento) {
        BLOCOS.register(barramento);
        ITENS.register(barramento);
    }

    private static boolean nunca(BlockState estado, BlockGetter nivel, BlockPos pos) {
        return false;
    }

    private static Block bloco(String id) {
        return ENTRADAS.get(id).bloco().get();
    }

    private static BlockBehaviour.Properties props(MaterialDecoracao material, int luz) {
        BlockBehaviour.Properties propriedades = material.propriedades();
        if (luz > 0) {
            propriedades.lightLevel(estado -> luz);
        }
        return propriedades;
    }

    private static BlockBehaviour.Properties translucido(MaterialDecoracao material, int luz) {
        return props(material, luz).noOcclusion().isRedstoneConductor(ModBlocosDecoracao::nunca)
                .isSuffocating(ModBlocosDecoracao::nunca).isViewBlocking(ModBlocosDecoracao::nunca);
    }

    private static DeferredBlock<Block> adicionar(String id, MaterialDecoracao material, Categoria categoria, Supplier<Block> fornecedor) {
        DeferredBlock<Block> bloco = BLOCOS.register(id, fornecedor);
        ENTRADAS.put(id, new Entrada(id, material, categoria, bloco));
        return bloco;
    }

    private static void comItem(String id, MaterialDecoracao material, Categoria categoria, Supplier<Block> fornecedor) {
        ITENS.registerSimpleBlockItem(adicionar(id, material, categoria, fornecedor));
    }

    private static void cubo(String id, MaterialDecoracao material, int luz) {
        comItem(id, material, Categoria.COMUM, () -> new Block(props(material, luz)));
    }

    private static void vidro(String id, MaterialDecoracao material, int luz) {
        comItem(id, material, Categoria.COMUM, () -> new TransparentBlock(translucido(material, luz)));
    }

    private static void vidroEixo(String id, MaterialDecoracao material) {
        comItem(id, material, Categoria.COMUM, () -> new BlocoVidroEixo(translucido(material, 0)));
    }

    private static void painel(String id, MaterialDecoracao material, int luz) {
        comItem(id, material, Categoria.COMUM, () -> new IronBarsBlock(props(material, luz).noOcclusion()));
    }

    private static void laje(String id, MaterialDecoracao material) {
        comItem(id, material, Categoria.LAJE, () -> new SlabBlock(props(material, 0)));
    }

    private static void escada(String id, String base, MaterialDecoracao material) {
        comItem(id, material, Categoria.ESCADA, () -> new StairBlock(bloco(base).defaultBlockState(), props(material, 0)));
    }

    private static void muro(String id, String base, MaterialDecoracao material) {
        comItem(id, material, Categoria.MURO, () -> new WallBlock(props(material, 0).forceSolidOn()));
    }

    private static void pilar(String id, MaterialDecoracao material) {
        comItem(id, material, Categoria.COMUM, () -> new RotatedPillarBlock(props(material, 0)));
    }

    private static void folhas(String id, MaterialDecoracao material) {
        comItem(id, material, Categoria.COMUM, () -> new Block(translucido(material, 0)));
    }

    private static void coluna(String id, MaterialDecoracao material) {
        comItem(id, material, Categoria.COMUM, () -> new BlocoColuna(props(material, 0), id));
    }

    private static void concha(String id, MaterialDecoracao material, String perola) {
        comItem(id, material, Categoria.COMUM, () -> new BlocoConcha(props(material, 0).noOcclusion(), id, () -> bloco(perola).asItem()));
    }

    private static void decor(String id, MaterialDecoracao material, TipoDecor tipo, int luz, int apoio) {
        boolean acendivel = tipo == TipoDecor.SIMPLES_LUZ || tipo == TipoDecor.HORIZONTAL_LUZ || tipo == TipoDecor.SEIS_LUZ;
        comItem(id, material, Categoria.COMUM, () -> {
            BlockBehaviour.Properties propriedades = material.propriedades().noOcclusion();
            if (acendivel) {
                propriedades.lightLevel(estado -> estado.getValue(BlocoDecorativoBase.ACESA) ? luz : 0);
            } else if (luz > 0) {
                propriedades.lightLevel(estado -> luz);
            }
            boolean semColisao = material.semColisao();
            return switch (tipo) {
                case SIMPLES -> new DecorSimples(propriedades, id, apoio, semColisao);
                case SIMPLES_LUZ -> new DecorSimplesLuz(propriedades, id, apoio, semColisao);
                case HORIZONTAL -> new DecorHorizontal(propriedades, id, apoio, semColisao);
                case HORIZONTAL_LUZ -> new DecorHorizontalLuz(propriedades, id, apoio, semColisao);
                case SEIS -> new DecorSeis(propriedades, id, apoio, semColisao);
                case SEIS_LUZ -> new DecorSeisLuz(propriedades, id, apoio, semColisao);
            };
        });
    }
}
