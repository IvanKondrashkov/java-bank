package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Update account balance (service-to-service call)"

    request {
        method PUT()
        urlPath("/accounts/ACC-001/balance")
        headers {
            contentType(applicationJson())
            header('Authorization', value(
                    consumer(regex('Bearer\\s+.+')),
                    producer('Bearer contract-test-token')
            ))
        }
        body(
                amount: $(consumer(anyNumber()), producer(100.0)),
                operationType: $(consumer(anyNonBlankString()), producer("DEPOSIT")),
                operationId: $(consumer(anyNonBlankString()), producer("op-1")),
                description: $(consumer(anyNonBlankString()), producer("Contract test deposit"))
        )
    }

    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(
                success: true,
                message: null,
                data: [
                        id          : $(consumer(anyNumber()), producer(1)),
                        accountNumber: $(consumer(anyNonBlankString()), producer("ACC-001")),
                        balance     : $(consumer(anyNumber()), producer(600.5)),
                        currency    : $(consumer(anyNonBlankString()), producer("RUB")),
                        status      : $(consumer(anyNonBlankString()), producer("ACTIVE"))
                ]
        )
    }
}
