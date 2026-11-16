package net.aelysium.aelysiummod.compatibilidade.articulacoes.acesso;

import net.minecraft.client.model.geom.ModelPart;
import net.aelysium.aelysiummod.compatibilidade.articulacoes.cuboide.CuboideMutavel;

import java.util.List;
import java.util.Optional;

public interface AcessoParteModelo {

    List<ModelPart.Cube> cpmec$obterCubos();

    void cpmec$definirCubos(List<ModelPart.Cube> cubos);

    static Optional<CuboideMutavel> primeiroCuboide(ModelPart parte) {
        if (parte == null) {
            return Optional.empty();
        }
        List<ModelPart.Cube> cubos = ((AcessoParteModelo) (Object) parte).cpmec$obterCubos();
        if (cubos == null || cubos.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of((CuboideMutavel) (Object) cubos.get(0));
    }
}
