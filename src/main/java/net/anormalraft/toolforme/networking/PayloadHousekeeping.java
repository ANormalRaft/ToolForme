package net.anormalraft.toolforme.networking;

import net.anormalraft.toolforme.networking.bindinghashmappayload.BindingsPayload;
import net.anormalraft.toolforme.networking.bindinghashmappayload.S2CBindingsPayloadHandler;
import net.anormalraft.toolforme.networking.formeitemtimerpayload.S2CFormeItemTimerPayloadHandler;
import net.anormalraft.toolforme.networking.formeitemtimerpayload.FormeItemTimerPayload;
import net.anormalraft.toolforme.networking.formeitemtimerpayload.C2SFormeItemTimerPayloadHandler;
import net.anormalraft.toolforme.networking.formeplayercooldownpayload.S2CFormePlayerCooldownPayloadHandler;
import net.anormalraft.toolforme.networking.formeplayercooldownpayload.FormePlayerCooldownPayload;
import net.anormalraft.toolforme.networking.formeplayercooldownpayload.C2SFormePlayerCooldownPayloadHandler;
import net.anormalraft.toolforme.networking.itemstackpayload.ItemStackPayload;
import net.anormalraft.toolforme.networking.itemstackpayload.C2SItemStackPayloadHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

//Where all payloads are registered
public class PayloadHousekeeping {

    //Registers Payloads
    public static void registerPayload(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(ItemStackPayload.TYPE, ItemStackPayload.STREAM_CODEC, C2SItemStackPayloadHandler::handleDataOnNetwork);
        registrar.playBidirectional(FormePlayerCooldownPayload.TYPE, FormePlayerCooldownPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        S2CFormePlayerCooldownPayloadHandler::handleDataOnNetwork,
                        C2SFormePlayerCooldownPayloadHandler::handleDataOnNetwork
                )
        );
        registrar.playBidirectional(
                FormeItemTimerPayload.TYPE,
                FormeItemTimerPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<>(
                        S2CFormeItemTimerPayloadHandler::handleDataOnNetwork,
                        C2SFormeItemTimerPayloadHandler::handleDataOnNetwork
                )
        );
        registrar.playToClient(BindingsPayload.TYPE, BindingsPayload.STREAM_CODEC, S2CBindingsPayloadHandler::handleDataOnNetwork
        );
    }
}
