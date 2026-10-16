package net.aelysium.aelysiummod.nucleo.armazenamento;

import com.google.gson.*;
import net.minecraft.nbt.*;

public class ConversorNbtJson {

    public static JsonElement paraJson(Tag tag) {
        if (tag instanceof CompoundTag composto) {
            JsonObject obj = new JsonObject();
            for (String chave : composto.getAllKeys()) obj.add(chave, paraJson(composto.get(chave)));
            return obj;
        }
        if (tag instanceof ListTag lista) {
            JsonArray arr = new JsonArray();
            for (Tag item : lista) arr.add(paraJson(item));
            return arr;
        }
        if (tag instanceof ByteArrayTag bytes) {
            JsonArray arr = new JsonArray();
            for (byte b : bytes.getAsByteArray()) arr.add(b);
            return arr;
        }
        if (tag instanceof IntArrayTag ints) {
            JsonArray arr = new JsonArray();
            for (int i : ints.getAsIntArray()) arr.add(i);
            return arr;
        }
        if (tag instanceof LongArrayTag longs) {
            JsonArray arr = new JsonArray();
            for (long l : longs.getAsLongArray()) arr.add(l);
            return arr;
        }
        if (tag instanceof StringTag texto) return new JsonPrimitive(texto.getAsString());
        if (tag instanceof NumericTag numero) {
            if (tag instanceof FloatTag || tag instanceof DoubleTag) return new JsonPrimitive(numero.getAsDouble());
            return new JsonPrimitive(numero.getAsLong());
        }
        return JsonNull.INSTANCE;
    }

    public static Tag deJson(JsonElement json) {
        if (json == null || json.isJsonNull()) return StringTag.valueOf("");
        if (json.isJsonObject()) {
            CompoundTag composto = new CompoundTag();
            for (var entrada : json.getAsJsonObject().entrySet()) composto.put(entrada.getKey(), deJson(entrada.getValue()));
            return composto;
        }
        if (json.isJsonArray()) {
            ListTag lista = new ListTag();
            for (JsonElement item : json.getAsJsonArray()) lista.add(deJson(item));
            return lista;
        }
        JsonPrimitive primitivo = json.getAsJsonPrimitive();
        if (primitivo.isBoolean()) return ByteTag.valueOf(primitivo.getAsBoolean());
        if (primitivo.isString()) return StringTag.valueOf(primitivo.getAsString());

        String bruto = primitivo.getAsString();
        if (bruto.contains(".") || bruto.contains("e") || bruto.contains("E")) return DoubleTag.valueOf(primitivo.getAsDouble());
        long valor = primitivo.getAsLong();
        if (valor >= Integer.MIN_VALUE && valor <= Integer.MAX_VALUE) return IntTag.valueOf((int) valor);
        return LongTag.valueOf(valor);
    }
}
