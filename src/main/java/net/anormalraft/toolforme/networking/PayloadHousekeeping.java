package net.anormalraft.toolforme.networking;

import net.anormalraft.toolforme.networking.bindinghashmappayload.BindingHashMapPayload;
import net.anormalraft.toolforme.networking.bindinghashmappayload.ClientBindingHashMapPayloadHandler;
import net.anormalraft.toolforme.networking.formeitemtimerpayload.ClientFormeItemTimerPayloadHandler;
import net.anormalraft.toolforme.networking.formeitemtimerpayload.FormeItemTimerPayload;
import net.anormalraft.toolforme.networking.formeitemtimerpayload.ServerFormeItemTimerPayloadHandler;
import net.anormalraft.toolforme.networking.formeplayercooldownpayload.ClientFormePlayerCooldownPayloadHandler;
import net.anormalraft.toolforme.networking.formeplayercooldownpayload.FormePlayerCooldownPayload;
import net.anormalraft.toolforme.networking.formeplayercooldownpayload.ServerFormePlayerCooldownPayloadHandler;
import net.anormalraft.toolforme.networking.itemstackpayload.ClientItemStackPayloadHandler;
import net.anormalraft.toolforme.networking.itemstackpayload.ItemStackPayload;
import net.anormalraft.toolforme.networking.itemstackpayload.ServerItemStackPayloadHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PayloadHousekeeping {

    //Registers Payloads
    public static void registerPayload(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(
                ItemStackPayload.TYPE,
                ItemStackPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<ItemStackPayload>(
                        ClientItemStackPayloadHandler::handleDataOnNetwork,
                        ServerItemStackPayloadHandler::handleDataOnNetwork
                )
        );
        registrar.playBidirectional(
                FormePlayerCooldownPayload.TYPE,
                FormePlayerCooldownPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<FormePlayerCooldownPayload>(
                        ClientFormePlayerCooldownPayloadHandler::handleDataOnNetwork,
                        ServerFormePlayerCooldownPayloadHandler::handleDataOnNetwork
                )
        );
        registrar.playBidirectional(
                FormeItemTimerPayload.TYPE,
                FormeItemTimerPayload.STREAM_CODEC,
                new DirectionalPayloadHandler<FormeItemTimerPayload>(
                        ClientFormeItemTimerPayloadHandler::handleDataOnNetwork,
                        ServerFormeItemTimerPayloadHandler::handleDataOnNetwork
                )
        );
        registrar.playToClient(BindingHashMapPayload.TYPE, BindingHashMapPayload.STREAM_CODEC, ClientBindingHashMapPayloadHandler::handleDataOnNetwork
        );
    }
}
