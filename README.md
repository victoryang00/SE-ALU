We focus on building an ALU, to be incorporated into the CPU pipeline, which
takes encrypted entries, compute on them, and encrypt the result. Using fresh
randomness on each encryption (64b value + 64b random pad), with good
probability, no two ciphertexts will be the same, regardless of operation, etc.
This makes the main challenge ensuring all operations take the same number of
cycles. We assume an RNG, a key-exchange protocol, and an encryption protocol.

Status:
- [x] AES model and tests working
- [ ] AES module working
- [x] AES Mock (to assume working)
- [x] ALU pipleine basic tests
- [x] ALU module assuming a working AES module
- [ ] ALU tests assuming a working AES module
