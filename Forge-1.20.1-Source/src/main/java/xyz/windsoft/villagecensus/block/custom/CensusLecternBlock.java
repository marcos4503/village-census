package xyz.windsoft.villagecensus.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import xyz.windsoft.villagecensus.block.ModBlockEntities;
import xyz.windsoft.villagecensus.block.entity.CensusLecternBlockEntity;

/*
 * This class creates the custom behavior for the block "Census Lectern"
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class CensusLecternBlock extends BaseEntityBlock {

    //Public static final variables
    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    //Public methods

    public CensusLecternBlock(Properties pProperties){
        //Repass the properties to parent class of this class
        super(pProperties);

        //Register the default state for this block
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        //Return the default state of placement, for this block. Make it face the Player
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        //Repass to parent class, the shape of model of this block
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        //Repass to parent class, the render shape type of this block
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        //Create and return a new Block Entity that will be placed and binded to this Block
        return new CensusLecternBlockEntity(pPos, pState);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        //Nothing to do when this Block breaks...

        //Repass to parent class, the call of this
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        //If is the client side, cancel here
        if (pLevel.isClientSide == true)
            return null;

        //Create and return a Ticker Helper for this Block, that will Tick the binded Block Entity of this Block
        return createTickerHelper(pBlockEntityType, ModBlockEntities.CENSUS_LECTERN_BLOCK_ENTITY.get(), ((pLevel1, pPos, pState1, pBlockEntity) -> {
            //Inform the tick method of the binded Block Entity, to be called
            pBlockEntity.Tick(pLevel1, pPos, pState1);
        }));
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        //If is the client side, cancel here
        if (pLevel.isClientSide() == true)
            return InteractionResult.sidedSuccess(pLevel.isClientSide());



        //Get the Block Entity binded to this Block
        BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
        //Try to run the interaction logic for this Block
        if (blockEntity instanceof CensusLecternBlockEntity){
            OnInteractWith(((CensusLecternBlockEntity) blockEntity), pState, pLevel, pPos, pPlayer, pHand, pHit);
        }
        else {
            throw new IllegalStateException("Container provider is missing!");
        }

        //Inform that the interaction was successfull
        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    //Private methods

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        //Add the property of FACING for the builder
        builder.add(FACING);
    }

    //Private auxiliar methods

    private void OnInteractWith(CensusLecternBlockEntity bindedBlockEntity, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit){
        //Repass this interaction to the Block Entity binded to this Block
        bindedBlockEntity.ReceivePlayerInteraction(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }
}