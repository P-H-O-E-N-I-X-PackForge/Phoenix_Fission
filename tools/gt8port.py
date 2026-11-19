import os, re, shutil, subprocess, sys

SRC = r"C:\Users\conno\Desktop\PFT Core\Phoenix's Fission"          # 1.21.1 working tree
MAIN120 = r"C:\Users\conno\Desktop\PFT Core\_wt120"                   # 1.20.1 worktree (destination + 1.20 builders)
S121 = SRC + r"\src\main\java\net\phoenix\phoenix_fission"
S120 = MAIN120 + r"\src\main\java\net\phoenix\phoenix_fission"
DST = MAIN120 + r"\src\gtm8\java\net\phoenix\phoenix_fission\gtm8"

SKIP_DIRS = {"configs", "gametest"}                     # shared with the GT7 set
SKIP_FILES = set()
FROM_120 = {"integration\\kubejs"}          # KubeJS 6 builders come from the 1.20.1 tree instead

SKIP_PREFIX = ("client\\render", "client\\Tokamak", "common\\AscensionReactorMachines", "api\\machine\\multiblock\\AscensionReactorMachine", "client\\gui\\Ascension")  # ported later

PKG = "net.phoenix.phoenix_fission"


def rewrite_pkg(s):
    # prefix every project package except the shared configs
    def rep(m):
        rest = m.group(1)
        if rest.startswith("configs"):
            return m.group(0)
        return PKG + ".gtm8." + rest
    s = re.sub(r"net\.phoenix\.phoenix_fission\.(?!gtm8)([A-Za-z_][\w\.]*)", rep, s)
    # a file in the root package
    s = re.sub(r"^package net\.phoenix\.phoenix_fission;", "package net.phoenix.phoenix_fission.gtm8;", s, flags=re.M)
    s = re.sub(r"import net\.phoenix\.phoenix_fission\.(?!gtm8)([A-Z]\w*);", PKG + r".gtm8.\1;", s) if False else s
    return s


def convert(s):
    s = rewrite_pkg(s)
    # NeoForge -> Forge
    s = s.replace("net.neoforged.neoforge.fluids.capability.IFluidHandler", "net.minecraftforge.fluids.capability.IFluidHandler")
    s = s.replace("net.neoforged.neoforge.fluids", "net.minecraftforge.fluids")
    s = s.replace("net.neoforged.neoforge.items", "net.minecraftforge.items")
    s = s.replace("net.neoforged.neoforge.common", "net.minecraftforge.common")
    s = s.replace("net.neoforged.neoforge.client", "net.minecraftforge.client")
    s = s.replace("net.neoforged.neoforge.event", "net.minecraftforge.event")
    s = s.replace("net.neoforged.neoforge.registries", "net.minecraftforge.registries")
    s = s.replace("net.neoforged.api.distmarker", "net.minecraftforge.api.distmarker")
    s = s.replace("net.neoforged.fml", "net.minecraftforge.fml")
    s = s.replace("net.neoforged.bus.api", "net.minecraftforge.eventbus.api")
    # registries
    s = re.sub(r"BuiltInRegistries\.(ITEM|BLOCK|FLUID)\.getOptional\(([^()]+)\)\.orElse\(null\)", lambda m: "ForgeRegistries.%sS.getValue(%s)" % (m.group(1) if m.group(1) != "FLUID" else "FLUID", m.group(2)), s)
    s = s.replace("ForgeRegistries.ITEMS", "ForgeRegistries.ITEMS").replace("ForgeRegistries.BLOCKS", "ForgeRegistries.BLOCKS")
    s = s.replace("BuiltInRegistries.ITEM", "ForgeRegistries.ITEMS").replace("BuiltInRegistries.BLOCK", "ForgeRegistries.BLOCKS").replace("BuiltInRegistries.FLUID", "ForgeRegistries.FLUIDS")
    s = s.replace("import net.minecraft.core.registries.BuiltInRegistries;", "import net.minecraftforge.registries.ForgeRegistries;")
    s = s.replace("ResourceLocation.parse(", "new ResourceLocation(")
    s = s.replace("net.neoforged.neoforge.common.NeoForge", "net.minecraftforge.common.MinecraftForge").replace("NeoForge.EVENT_BUS", "MinecraftForge.EVENT_BUS")
    # GT8 on 1.20.1 keeps the older registrate: definitions and recipe types are returned directly
    s = re.sub(r"MachineEntry<(\w+)>", lambda m: m.group(1), s)
    s = s.replace("GTRecipeTypeEntry", "GTRecipeType")
    s = re.sub(r"import com\.gregtechceu\.gtceu\.api\.registry\.registrate\.entry\.\w+;" + chr(10), "", s)
    s = s.replace("import com.gregtechceu.gtceu.api.registry.registrate.builder.MachineBuilder;", "import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;")
    # 1.20 tooltip signatures
    if "extends Block" in s or "extends ActiveBlock" in s or "extends FusionCoilBlock" in s:
        s = s.replace("Item.TooltipContext context", "@javax.annotation.Nullable net.minecraft.world.level.BlockGetter level")
    else:
        s = s.replace("Item.TooltipContext context", "@javax.annotation.Nullable net.minecraft.world.level.Level level")
    s = s.replace("RecipeOutput provider", "java.util.function.Consumer<net.minecraft.data.recipes.FinishedRecipe> provider")
    s = s.replace("import net.minecraft.data.recipes.RecipeOutput;", "import net.minecraft.data.recipes.FinishedRecipe;")
    s = re.sub(r"(\w+)\.copyWithAmount\(([^()]+)\)", lambda m: "new FluidStack(" + m.group(1) + ", " + m.group(2) + ")", s)
    s = s.replace("GTRecipeSerializer.CODEC.codec()", "GTRecipeSerializer.CODEC")
    return s


def process(src_root, rel, dst_root):
    sp = os.path.join(src_root, rel)
    dp = os.path.join(dst_root, rel)
    os.makedirs(os.path.dirname(dp), exist_ok=True)
    s = open(sp, encoding="utf-8").read()
    open(dp, "w", encoding="utf-8").write(convert(s))


count = 0
for root, dirs, files in os.walk(S121):
    rel_dir = os.path.relpath(root, S121)
    top = rel_dir.split(os.sep)[0]
    if top in SKIP_DIRS:
        continue
    for f in files:
        if not f.endswith(".java"):
            continue
        rel = os.path.normpath(os.path.join(rel_dir, f)) if rel_dir != "." else f
        if rel in SKIP_FILES:
            continue
        if any(rel.startswith(p) for p in FROM_120) or any(rel.startswith(p) for p in SKIP_PREFIX):
            continue
        process(S121, rel, DST)
        count += 1

# the 1.20.1 KubeJS builders and plugin
for root, dirs, files in os.walk(S120 + r"\integration\kubejs"):
    for f in files:
        if f.endswith(".java"):
            rel = os.path.relpath(os.path.join(root, f), S120)
            process(S120, rel, DST)
            count += 1
print(count, "files converted")
